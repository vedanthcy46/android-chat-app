const express = require("express");
const router = express.Router();
const chatController = require("../controllers/chat.controller");
const { protectRoute } = require("../middleware/auth.middleware");

router.post("/conversation", protectRoute, chatController.getConversation);
router.get("/conversations", protectRoute, chatController.getUserConversations);
router.post("/message", protectRoute, chatController.sendMessage);
router.get("/messages/:conversationId", protectRoute, chatController.getMessages);
router.put("/read/:conversationId", protectRoute, chatController.markAsRead);
router.get("/unread-count", protectRoute, chatController.getUnreadCount);

module.exports = router;
