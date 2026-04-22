const Post = require("../models/post.model");
const User = require("../models/user.model");
const logger = require("../utils/logger");
const tryCatch = require("../utils/tryCatch");

exports.uploadImage = tryCatch(async (req, res) => {
  if (!req.file) {
    logger.error("Upload Error: No file in request");
    return res.status(400).json({ message: "No image file provided" });
  }
  
  // Cloudinary storage provides 'path', 'url', or 'secure_url'
  const imageUrl = req.file.path || req.file.url || req.file.secure_url;
  
  if (!imageUrl) {
    logger.error(`Upload Error: File uploaded but no URL found in: ${JSON.stringify(req.file)}`);
    return res.status(500).json({ message: "Image uploaded but URL not found" });
  }

  logger.info(`Image uploaded successfully: ${imageUrl}`);
  res.status(200).json({ url: imageUrl });
});

exports.createPost = tryCatch(async (req, res) => {
  const { caption, image } = req.body;
  
  const imageUrl = req.file ? (req.file.path || req.file.url || req.file.secure_url) : image;

  if (!imageUrl) {
    return res.status(400).json({ message: "Image is required" });
  }

  const newPost = new Post({
    user: req.user._id,
    image: imageUrl,
    caption: caption || ""
  });

  try {
    await newPost.save();
    const populatedPost = await Post.findById(newPost._id).populate("user", "username profilePic");
    logger.info(`New post created by user: ${req.user.username}`);
    res.status(201).json(populatedPost);
  } catch (err) {
    logger.error(`Post Save Error: ${err.message}`);
    res.status(500).json({ message: "Failed to save post", error: err.message });
  }
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
