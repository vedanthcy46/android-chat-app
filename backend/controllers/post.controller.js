const Post = require("../models/post.model");
const User = require("../models/user.model");
const logger = require("../utils/logger");
const tryCatch = require("../utils/tryCatch");

exports.uploadImage = tryCatch(async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ message: "No image file provided" });
  }
  res.status(200).json({ url: req.file.path });
});

exports.createPost = tryCatch(async (req, res) => {
  const { caption } = req.body;
  if (!req.file) {
    return res.status(400).json({ message: "Image is required" });
  }

  const newPost = new Post({
    user: req.user._id,
    image: req.file.path,
    caption
  });

  await newPost.save();
  logger.info(`New post created by user: ${req.user.username}`);
  res.status(201).json(newPost);
});

exports.getAllPosts = tryCatch(async (req, res) => {
  const posts = await Post.find()
    .populate("user", "username profilePic")
    .sort({ createdAt: -1 });
  res.status(200).json(posts);
});

exports.getFeed = tryCatch(async (req, res) => {
  const currentUser = await User.findById(req.user._id);
  const followingIds = currentUser.following;

  const feedPosts = await Post.find({ user: { $in: [...followingIds, req.user._id] } })
    .populate("user", "username profilePic")
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
  res.status(200).json({ message: isLiked ? "Unliked" : "Liked", likesCount: post.likes.length });
});

exports.savePost = tryCatch(async (req, res) => {
  const user = await User.findById(req.user._id);
  const postId = req.params.id;

  const isSaved = user.savedPosts.includes(postId);

  if (isSaved) {
    user.savedPosts = user.savedPosts.filter(id => id.toString() !== postId);
  } else {
    user.savedPosts.push(postId);
  }

  await user.save();
  res.status(200).json({ message: isSaved ? "Unsaved" : "Saved" });
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
  res.status(201).json({ message: "Comment added", comments: post.comments });
});
