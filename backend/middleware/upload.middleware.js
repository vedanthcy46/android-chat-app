const cloudinary = require('cloudinary');
const cloudinaryStorage = require('multer-storage-cloudinary');
const multer = require('multer');

cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
});

const storage = cloudinaryStorage({
  cloudinary: cloudinary,
  params: {
    folder: 'social_app_posts',
    allowed_formats: ['jpg', 'png', 'jpeg', 'mp4', 'mov', 'avi'],
    resource_type: 'auto', // Important for video support
  }
});

const upload = multer({ storage: storage });

module.exports = upload;
