const User = require("../models/user.model");
const logger = require("../utils/logger");
const tryCatch = require("../utils/tryCatch");

exports.getProfileById = tryCatch(async (req, res) => {
  const { id } = req.params;
  const user = await User.findById(id).select("-password").populate("followers following", "username profilePic");
  
  if (!user) {
    return res.status(404).json({ message: "User not found" });
  }
  
  res.status(200).json(user);
});

exports.searchUsers = tryCatch(async (req, res) => {
  const { q } = req.query;
  if (!q) return res.status(400).json({ message: "Search query is required" });

  const users = await User.find({
    username: { $regex: q, $options: "i" }
  }).select("username profilePic bio");

  res.status(200).json(users);
});

exports.updateProfile = tryCatch(async (req, res) => {
  const { username, bio, profilePic } = req.body;
  const user = await User.findById(req.user._id);

  if (username) {
    const existingUser = await User.findOne({ username });
    if (existingUser && existingUser._id.toString() !== req.user._id.toString()) {
      return res.status(400).json({ message: "Username already taken" });
    }
    user.username = username;
  }
  if (bio) user.bio = bio;
  if (profilePic) user.profilePic = profilePic;

  await user.save();
  res.status(200).json({ message: "Profile updated", user });
});

exports.followUnfollowUser = tryCatch(async (req, res) => {
  const { id } = req.params;
  const userToFollow = await User.findById(id);
  const currentUser = await User.findById(req.user._id);

  if (id === req.user._id.toString()) {
    return res.status(400).json({ message: "You cannot follow yourself" });
  }

  if (!userToFollow || !currentUser) {
    return res.status(404).json({ message: "User not found" });
  }

  const isFollowing = currentUser.following.includes(id);

  if (isFollowing) {
    currentUser.following = currentUser.following.filter(fId => fId.toString() !== id);
    userToFollow.followers = userToFollow.followers.filter(fId => fId.toString() !== req.user._id.toString());
    res.status(200).json({ message: "Unfollowed successfully" });
  } else {
    currentUser.following.push(id);
    userToFollow.followers.push(req.user._id);
    res.status(200).json({ message: "Followed successfully" });
  }

  await currentUser.save();
  await userToFollow.save();
});
