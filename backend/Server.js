require('dotenv').config();
const express = require("express");
const cors = require("cors");
const http = require("http");
const { Server } = require("socket.io");
const DbConnect = require("./config/db");
const authRoutes = require("./routes/auth.route");
const userRoutes = require("./routes/user.route");
const postRoutes = require("./routes/post.route");
const chatRoutes = require("./routes/chat.route");
const errorHandler = require("./middleware/error.middleware");
const logger = require("./utils/logger");
const Message = require("./models/message.model");
const Conversation = require("./models/conversation.model");
const User = require("./models/user.model");


const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

const app = express();

// Security Middleware
app.use(helmet()); // Secure HTTP headers

const limiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute for testing
  max: 100, // Limit each IP to 100 requests per windowMs
  message: "Too many requests from this IP, please try again after 1 minute",
});
app.use("/api/", limiter); // Apply rate limiting to all API routes
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: "*", // Adjust as needed for production
    methods: ["GET", "POST"]
  }
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Force UTF-8 for all JSON responses
app.use((req, res, next) => {
  res.header("Content-Type", "application/json; charset=utf-8");
  next();
});

// Routes
app.get("/", (req, res) => {
  res.send("Social Media API is running...");
});

app.use("/api/auth", authRoutes);
app.use("/api/users", userRoutes);
app.use("/api/posts", postRoutes);
app.use("/api/chat", chatRoutes);

// Socket.IO for Real-time Chat
const userSocketMap = {}; // {userId: socketId}
const activeChatMap = {}; // {socketId: conversationId} - tracks which chat a user is currently viewing

io.on("connection", async (socket) => {
  const userId = socket.handshake.query.userId;
  if (userId) {
    userSocketMap[userId] = socket.id;
    
    // Update user status to online
    try {
      await User.findByIdAndUpdate(userId, { isOnline: true });
      logger.info(`Broadcasting online status for ${userId}`);
      io.emit("userStatusChanged", { userId, isOnline: true });
    } catch (err) {
      logger.error(`Error updating online status: ${err.message}`);
    }
  }

  logger.info(`User connected: ${userId} (Socket: ${socket.id})`);

  // Track which chat the user is currently looking at
  socket.on("joinChat", ({ conversationId }) => {
    activeChatMap[socket.id] = conversationId;
    logger.debug(`User ${userId} joined chat ${conversationId}`);
  });

  socket.on("leaveChat", () => {
    delete activeChatMap[socket.id];
  });

  socket.on("sendMessage", async ({ conversationId, senderId, receiverId, text }) => {
    try {
      const receiverSocketId = userSocketMap[receiverId];
      const isReceiverInChat = activeChatMap[receiverSocketId] === conversationId;

      const message = new Message({ 
        conversationId, 
        senderId, 
        text,
        isRead: isReceiverInChat // Mark read immediately if receiver is in this chat
      });
      await message.save();

      // Update conversation with last message reference
      await Conversation.findByIdAndUpdate(conversationId, {
        lastMessage: message._id
      });

      if (receiverSocketId) {
        io.to(receiverSocketId).emit("newMessage", message);
      }
      
      // Also emit back to sender to confirm saved/delivered
      socket.emit("messageSent", message);

    } catch (err) {
      logger.error(`Socket Error: ${err.message}`);
    }
  });

  socket.on("disconnect", async () => {
    if (userId) {
      delete userSocketMap[userId];
      delete activeChatMap[socket.id];
      
      // Update user status to offline
      try {
        await User.findByIdAndUpdate(userId, { isOnline: false, lastSeen: Date.now() });
        logger.info(`Broadcasting offline status for ${userId}`);
        io.emit("userStatusChanged", { userId, isOnline: false, lastSeen: new Date().toISOString() });
      } catch (err) {
        logger.error(`Error updating offline status: ${err.message}`);
      }
    }
    logger.info(`User disconnected: ${userId}`);
  });
});

// Error Handler Middleware (MUST be last)
app.use(errorHandler);

const PORT = process.env.PORT || 5000;

// Connect to DB and start server
DbConnect().then(() => {
  server.listen(PORT, () => {
    logger.info(`Server running on port ${PORT}`);
  });
}).catch((err) => {
  logger.error(`Failed to start server: ${err.message}`);
  process.exit(1);
});
