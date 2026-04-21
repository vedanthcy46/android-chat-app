const express = require("express");
const router = express.Router();
const chatController = require("../controllers/chat.controller");
const { protectRoute } = require("../middleware/auth.middleware");

router.post("/conversation", protectRoute, chatController.getConversation);
router.get("/conversation/:userId", protectRoute, chatController.getUserConversations);
router.post("/message", protectRoute, chatController.sendMessage);
router.get("/message/:conversationId", protectRoute, chatController.getMessages);

module.exports = router;
