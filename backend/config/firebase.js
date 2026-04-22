const admin = require("firebase-admin");
const logger = require("../utils/logger");

// NOTE: You must place your 'serviceAccountKey.json' in the backend root directory.
// You can download this from Firebase Console -> Project Settings -> Service Accounts.
let serviceAccount;
try {
  serviceAccount = require("../serviceAccountKey.json");
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  logger.info("Firebase Admin initialized successfully.");
} catch (error) {
  logger.warn("Firebase serviceAccountKey.json not found. Push notifications will be disabled.");
}

/**
 * Sends a push notification to a specific device.
 * @param {string} token - The FCM token of the receiver.
 * @param {string} title - Notification title.
 * @param {string} body - Notification body.
 * @param {object} data - Custom data payload (type, id, etc).
 */
const sendPushNotification = async (token, title, body, data = {}) => {
  if (admin.apps.length === 0) {
    logger.warn("Firebase Admin not initialized. Skipping notification.");
    return;
  }
  
  if (!token) {
    logger.warn("Attempted to send notification but FCM token is missing.");
    return;
  }

  const message = {
    notification: {
      title,
      body,
    },
    data: data,
    token: token,
  };

  try {
    const response = await admin.messaging().send(message);
    logger.info(`Successfully sent push notification: ${response}`);
  } catch (error) {
    logger.error(`Error sending push notification: ${error.message}`);
  }
};

module.exports = { sendPushNotification };
