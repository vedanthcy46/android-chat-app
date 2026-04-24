const Message = require("../models/message.model");
const Conversation = require("../models/conversation.model");
const User = require("../models/user.model");
const { sendPushNotification } = require("../config/firebase");

// Create or Get Conversation
exports.getConversation = async (req, res, next) => {
  try {
    const { receiverId } = req.body;
    const senderId = req.user._id;

    let conversation = await Conversation.findOne({
      members: { $all: [senderId, receiverId] }
    });

    if (!conversation) {
      conversation = new Conversation({
        members: [senderId, receiverId]
      });
      await conversation.save();
    }

    // Populate members and lastMessage to match the Android data model
    const populatedConversation = await Conversation.findById(conversation._id)
      .populate("members", "username profilePic")
      .populate("lastMessage");

    res.status(200).json(populatedConversation);
  } catch (err) {
    next(err);
  }
};

// Get all conversations for a user with unread counts
exports.getUserConversations = async (req, res, next) => {
  try {
    const userId = req.user._id;
    const conversations = await Conversation.find({
      members: { $in: [userId] }
    })
    .populate("members", "username profilePic isOnline lastSeen")
    .populate("lastMessage")
    .sort({ updatedAt: -1 });

    // Calculate unread counts for each conversation
    const conversationsWithUnread = await Promise.all(conversations.map(async (conv) => {
      const unreadCount = await Message.countDocuments({
        conversationId: conv._id,
        senderId: { $ne: userId },
        isRead: false
      });
      return {
        ...conv.toObject(),
        unreadCount
      };
    }));

    res.status(200).json(conversationsWithUnread);
  } catch (err) {
    next(err);
  }
};

// Send a message via HTTP
exports.sendMessage = async (req, res, next) => {
  try {
    const { conversationId, text } = req.body;
    const senderId = req.user._id;

    // Determine if receiver is currently in this chat (if we had access to activeChatMap here)
    // For HTTP, we default to false, or check if we can pass more info
    const newMessage = new Message({
      conversationId,
      senderId,
      text,
      isRead: false 
    });
    await newMessage.save();

    // Update conversation with last message reference
    const conversation = await Conversation.findByIdAndUpdate(conversationId, {
      lastMessage: newMessage._id,
      updatedAt: Date.now()
    });

    // Send Push Notification to receiver
    if (conversation) {
      const receiverId = conversation.members.find(id => id.toString() !== senderId.toString());
      if (receiverId) {
        const receiver = await User.findById(receiverId);
        if (receiver && receiver.fcmToken) {
          sendPushNotification(
            receiver.fcmToken,
            "New Message",
            `${req.user.username}: ${text}`,
            {
              type: "message",
              conversationId: conversationId,
              senderId: senderId.toString()
            }
          );
        }
      }
    }

    res.status(201).json(newMessage);
  } catch (err) {
    next(err);
  }
};

// Mark all messages in a conversation as read
exports.markAsRead = async (req, res, next) => {
  try {
    const { conversationId } = req.params;
    const userId = req.user._id;

    await Message.updateMany(
      { conversationId, senderId: { $ne: userId }, isRead: false },
      { $set: { isRead: true } }
    );

    res.status(200).json({ message: "Messages marked as read" });
  } catch (err) {
    next(err);
  }
};

// Get total unread message count for a user
exports.getUnreadCount = async (req, res, next) => {
  try {
    const userId = req.user._id;
    const unreadCount = await Message.countDocuments({
      senderId: { $ne: userId },
      isRead: false
    });
    res.status(200).json({ totalUnread: unreadCount });
  } catch (err) {
    next(err);
  }
};

// Get Messages in a Conversation
exports.getMessages = async (req, res, next) => {
  try {
    const { conversationId } = req.params;
    const messages = await Message.find({ conversationId }).sort({ createdAt: 1 });
    res.status(200).json(messages);
  } catch (err) {
    next(err);
  }
};
