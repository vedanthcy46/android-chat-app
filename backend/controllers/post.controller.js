const Post = require("../models/post.model");
const User = require("../models/user.model");
const logger = require("../utils/logger");
const tryCatch = require("../utils/tryCatch");

exports.uploadImage = tryCatch(async (req, res) => {
  if (!req.file) {
    logger.error("Upload Error: No file in request");
    return res.status(400).json({ message: "No media file provided" });
  }
  
  const imageUrl = req.file.path || req.file.url || req.file.secure_url;
  
  if (!imageUrl) {
    logger.error(`Upload Error: File uploaded but no URL found`);
    return res.status(500).json({ message: "Upload failed to return URL" });
  }

  // Detect type
  const postType = (req.file.mimetype.startsWith("video/") || /\.(mp4|mov|avi|mkv|webm)$/i.test(req.file.originalname)) ? "video" : "image";

  logger.info(`${postType} uploaded successfully: ${imageUrl}`);
  res.status(200).json({ url: imageUrl, postType: postType });
});

exports.createPost = tryCatch(async (req, res) => {
  const { caption, image, videoUrl: bodyVideoUrl, postType: bodyPostType } = req.body;
  
  let imageUrl = req.file ? (req.file.path || req.file.url || req.file.secure_url) : image;
  let videoUrl = bodyVideoUrl || "";
  let postType = bodyPostType || "image";

  // Detailed Debugging log for incoming file
  if (req.file) {
    logger.info(`Create Post - File Received: ${req.file.originalname}, Mimetype: ${req.file.mimetype}, Size: ${req.file.size}`);
    logger.debug(`Full File Object: ${JSON.stringify(req.file)}`);
  }

  // Robust video detection: Check mimetype OR file extension OR the URL itself (for 2-step process)
  const isVideoFile = (req.file && (
    req.file.mimetype.startsWith("video/") || 
    /\.(mp4|mov|avi|mkv|webm)$/i.test(req.file.originalname)
  )) || (imageUrl && (/\.(mp4|mov|avi|mkv|webm)(\?.*)?$/i.test(imageUrl) || imageUrl.includes("/video/upload/")));

  if (isVideoFile) {
    postType = "video";
    videoUrl = imageUrl; 
    logger.info(`Video confirmed. URL set to: ${videoUrl}`);
  }

  if (!imageUrl && !videoUrl) {
    logger.error("Create Post Error: No media provided");
    return res.status(400).json({ message: "Media (image or video) is required" });
  }

  const newPost = new Post({
    user: req.user._id,
    image: imageUrl || videoUrl, // Use video URL as thumbnail placeholder if image is missing
    videoUrl: videoUrl,
    postType: postType,
    caption: caption || ""
  });

  try {
    await newPost.save();
    const populatedPost = await Post.findById(newPost._id).populate("user", "username profilePic");
    logger.info(`Successfully created ${postType} post. ID: ${newPost._id}`);
    res.status(201).json(populatedPost);
  } catch (err) {
    logger.error(`Post Save Error: ${err.message}`);
    res.status(500).json({ message: "Failed to save post", error: err.message });
  }
});

exports.getReels = tryCatch(async (req, res) => {
  logger.info("Fetching Reels (video-only posts)");
  const reels = await Post.find({ postType: "video" })
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic")
    .sort({ createdAt: -1 });
  
  logger.info(`Found ${reels.length} reels`);
  res.status(200).json(reels);
});

exports.getAllPosts = tryCatch(async (req, res) => {
  const posts = await Post.find()
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic")
    .sort({ createdAt: -1 });
  res.status(200).json(posts);
});

exports.getFeed = tryCatch(async (req, res) => {
  const currentUser = await User.findById(req.user._id);
  const followingIds = currentUser.following;

  const feedPosts = await Post.find({ user: { $in: [...followingIds, req.user._id] } })
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic")
    .sort({ createdAt: -1 });

  res.status(200).json(feedPosts);
});

exports.likePost = tryCatch(async (req, res) => {
  const post = await Post.findById(req.params.id);
  if (!post) return res.status(404).json({ message: "Post not found" });

  const isLiked = post.likes.includes(req.user._id);

  if (isLiked) {
    post.likes = post.likes.filter(id => id.toString() !== req.user._id.toString());
  } else {
    post.likes.push(req.user._id);
  }

  await post.save();
  const updatedPost = await Post.findById(post._id).populate("user", "username profilePic");
  res.status(200).json(updatedPost);
});

exports.savePost = tryCatch(async (req, res) => {
  const user = await User.findById(req.user._id);
  const post = await Post.findById(req.params.id);
  
  if (!post) return res.status(404).json({ message: "Post not found" });

  const isSavedInUser = user.savedPosts.includes(post._id);
  const isSavedInPost = post.saves.includes(req.user._id);

  if (isSavedInUser) {
    user.savedPosts = user.savedPosts.filter(id => id.toString() !== post._id.toString());
    post.saves = post.saves.filter(id => id.toString() !== req.user._id.toString());
  } else {
    user.savedPosts.push(post._id);
    post.saves.push(req.user._id);
  }

  await user.save();
  await post.save();

  const updatedPost = await Post.findById(post._id).populate("user", "username profilePic");
  res.status(200).json(updatedPost);
});

exports.getPostById = tryCatch(async (req, res) => {
  const post = await Post.findById(req.params.id)
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic");
    
  if (!post) return res.status(404).json({ message: "Post not found" });
  res.status(200).json(post);
});

exports.addComment = tryCatch(async (req, res) => {
  const { text } = req.body;
  const post = await Post.findById(req.params.id);
  if (!post) return res.status(404).json({ message: "Post not found" });

  post.comments.push({
    user: req.user._id,
    text
  });

  await post.save();
  const updatedPost = await Post.findById(post._id)
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic");
  res.status(201).json(updatedPost);
});

exports.getUserPosts = tryCatch(async (req, res) => {
  const { userId } = req.params;
  const posts = await Post.find({ user: userId })
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic")
    .sort({ createdAt: -1 });
  res.status(200).json(posts);
});

exports.addReply = tryCatch(async (req, res) => {
  const { text, commentId } = req.body;
  const post = await Post.findById(req.params.id);
  if (!post) return res.status(404).json({ message: "Post not found" });

  const comment = post.comments.id(commentId);
  if (!comment) return res.status(404).json({ message: "Comment not found" });

  comment.replies.push({
    user: req.user._id,
    text
  });

  await post.save();
  const updatedPost = await Post.findById(post._id)
    .populate("user", "username profilePic")
    .populate("comments.user", "username profilePic")
    .populate("comments.replies.user", "username profilePic");
  res.status(201).json(updatedPost);
});

exports.deletePost = tryCatch(async (req, res) => {
  const post = await Post.findById(req.params.id);
  if (!post) return res.status(404).json({ message: "Post not found" });

  // Check ownership
  if (post.user.toString() !== req.user._id.toString()) {
    return res.status(403).json({ message: "You can only delete your own posts" });
  }

  await Post.findByIdAndDelete(req.params.id);
  logger.info(`Post deleted: ${req.params.id} by user: ${req.user._id}`);
  res.status(200).json({ message: "Post deleted successfully" });
});
