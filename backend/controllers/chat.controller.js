const Message = require("../models/message.model");
const Conversation = require("../models/conversation.model");

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

    res.status(200).json(conversation);
  } catch (err) {
    next(err);
  }
};

// Get all conversations for a user
exports.getUserConversations = async (req, res, next) => {
  try {
    const conversations = await Conversation.find({
      members: { $in: [req.user._id] }
    }).populate("members", "username profilePic");
    res.status(200).json(conversations);
  } catch (err) {
    next(err);
  }
};

// Send a message via HTTP
exports.sendMessage = async (req, res, next) => {
  try {
    const { conversationId, text } = req.body;
    const newMessage = new Message({
      conversationId,
      senderId: req.user._id,
      text
    });
    await newMessage.save();
    res.status(201).json(newMessage);
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
