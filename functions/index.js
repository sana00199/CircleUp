const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendFcmNotification = functions.database
  .ref('/fcm_requests/{requestId}')
  .onCreate(async (snapshot, context) => {
    const requestData = snapshot.val();
    
    if (!requestData || !requestData.payload) {
      console.error('Invalid FCM request data');
      return null;
    }
    
    try {
      const payload = JSON.parse(requestData.payload);
      
      // Send the message
      const response = await admin.messaging().send({
        token: payload.to,
        notification: payload.notification,
        data: payload.data,
        android: {
          priority: 'high'
        },
        apns: {
          headers: {
            'apns-priority': '10'
          }
        }
      });
      
      console.log('Successfully sent message:', response);
      
      // Delete the request after processing
      return snapshot.ref.remove();
      
    } catch (error) {
      console.error('Error sending message:', error);
      return null;
    }
  });

