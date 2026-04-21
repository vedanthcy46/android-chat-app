const express = require("express");
const router = express.Router();
const postController = require("../controllers/post.controller");
const { protectRoute } = require("../middleware/auth.middleware");
const upload = require("../middleware/upload.middleware");

router.post("/upload", protectRoute, upload.single("image"), postController.uploadImage);
router.post("/create", protectRoute, upload.single("image"), postController.createPost);
router.get("/all", protectRoute, postController.getAllPosts);
router.get("/feed", protectRoute, postController.getFeed);
router.put("/like/:id", protectRoute, postController.likePost);
router.put("/save/:id", protectRoute, postController.savePost);
router.post("/comment/:id", protectRoute, postController.addComment);

module.exports = router;
