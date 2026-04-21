# Social Media Backend API

A robust, scalable backend for a social media application (Instagram clone) built with Node.js, Express, MongoDB, and Socket.IO.

## 🚀 Tech Stack
- **Server**: Node.js, Express.js
- **Database**: MongoDB with Mongoose
- **Authentication**: JWT & Bcryptjs
- **Real-time**: Socket.IO
- **File Storage**: Cloudinary & Multer
- **Security**: Helmet, Express Rate Limit, Express Validator

## 🛠️ Getting Started

### 1. Prerequisites
- Node.js installed
- MongoDB URI (Local or Atlas)
- Cloudinary Account

### 2. Installation
```bash
cd backend
npm install
```

### 3. Environment Variables
Create a `.env` file in the `backend` folder:
```env
PORT=5000
MONGODB_URL=your_mongodb_uri
JWT_SECRET=your_secret_key
CLOUDINARY_CLOUD_NAME=your_name
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret
NODE_ENV=development
```

### 4. Run the server
```bash
npm start
```

---

## 📡 API Reference

### 🔐 Authentication (`/api/auth`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/register` | `POST` | Register a new user |
| `/login` | `POST` | Login & receive JWT token |

### 👤 User Profile (`/api/users`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/:id` | `GET` | Get user details by ID |
| `/search?q=` | `GET` | Search users by username |
| `/update` | `PUT` | Update bio, profilePic, username |
| `/follow/:id` | `POST` | Follow/Unfollow a user |

### 📸 Posts (`/api/posts`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/create` | `POST` | Create post (Caption + Image) |
| `/upload` | `POST` | Upload image & get URL |
| `/feed` | `GET` | Get posts from followed users |
| `/all` | `GET` | Get all posts |
| `/like/:id` | `PUT` | Like/Unlike a post |
| `/save/:id` | `PUT` | Save/Unsave a post |
| `/comment/:id` | `POST` | Add a comment |

### 💬 Chat (`/api/chat`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/conversation` | `POST` | Create/Get a conversation |
| `/conversation/:userId` | `GET` | Get all user conversations |
| `/message` | `POST` | Send a message via HTTP |
| `/message/:conversationId` | `GET` | Get message history |

---

## 🔌 Socket.IO Events

**Connection**: `io("http://localhost:5000", { query: { userId: "ID" } })`

| Event | Type | Data | Description |
| :--- | :--- | :--- | :--- |
| `sendMessage` | `Emit` | `{ conversationId, senderId, receiverId, text }` | Send a live message |
| `newMessage` | `Listen`| `{ _id, conversationId, senderId, text, ... }` | Receive a live message |

---

## 🛡️ Security Features
- **Helmet**: Secured HTTP headers.
- **Rate Limiting**: 100 requests per 15 minutes per IP.
- **Input Validation**: Strict validation for Register and Login data.
- **Error Handling**: Centralized error responses with stack traces hidden in production.
