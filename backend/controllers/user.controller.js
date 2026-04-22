const User = require("../models/user.model");
const logger = require("../utils/logger");
const tryCatch = require("../utils/tryCatch");
const { sendPushNotification } = require("../config/firebase");

exports.getUserProfile = tryCatch(async (req, res) => {
  const user = await User.findById(req.params.id).select("-password");
  
  if (!user) return res.status(404).json({ message: "User not found" });
  res.status(200).json(user);
});

exports.getUsersByIds = tryCatch(async (req, res) => {
  const { ids } = req.query;
  if (!ids) return res.status(200).json([]);
  
  const idArray = ids.split(",");
  const users = await User.find({ _id: { $in: idArray } }).select("username profilePic email");
  res.status(200).json(users);
});

exports.searchUsers = tryCatch(async (req, res) => {
  const { q } = req.query;
  if (!q) return res.status(400).json({ message: "Search query is required" });

  const escapedQuery = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const users = await User.find({
    username: { $regex: escapedQuery, $options: "i" }
  }).select("username email profilePic bio followers following");

  res.status(200).json(users);
});

exports.updateProfile = tryCatch(async (req, res) => {
  const { username, bio, profilePic } = req.body;
  const user = await User.findById(req.user._id);

  if (username !== undefined) {
    if (username.length > 0) {
      const existingUser = await User.findOne({ username });
      if (existingUser && existingUser._id.toString() !== req.user._id.toString()) {
        return res.status(400).json({ message: "Username already taken" });
      }
      user.username = username;
    }
  }
  
  if (bio !== undefined) user.bio = bio;
  if (profilePic !== undefined) user.profilePic = profilePic;

  await user.save();
  res.status(200).json(user);
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
    
    // Send Push Notification
    if (userToFollow.fcmToken) {
      sendPushNotification(
        userToFollow.fcmToken,
        "New Follower",
        `${currentUser.username} started following you`,
        {
          type: "follow",
          userId: req.user._id.toString()
        }
      );
    }

    res.status(200).json({ message: "Followed successfully" });
  }

  await currentUser.save();
  await userToFollow.save();
});

exports.updateFcmToken = tryCatch(async (req, res) => {
  const { token } = req.body;
  if (!token) return res.status(400).json({ message: "Token is required" });

  await User.findByIdAndUpdate(req.user._id, { fcmToken: token });
  res.status(200).json({ message: "FCM Token updated successfully" });
});
