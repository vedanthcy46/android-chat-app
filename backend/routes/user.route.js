const express = require("express");
const router = express.Router();
const userController = require("../controllers/user.controller");
const { protectRoute } = require("../middleware/auth.middleware");

// Profile Routes
router.get("/search", protectRoute, userController.searchUsers);
router.get("/list", protectRoute, userController.getUsersByIds);
router.get("/:id", protectRoute, userController.getProfileById); 
router.put("/update", protectRoute, userController.updateProfile);
router.post("/follow/:id", protectRoute, userController.followUnfollowUser);

module.exports = router;
