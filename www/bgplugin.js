var bgplugin = {
        createEvent: function(contactId,successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'BGPlugin', // mapped to our native Java class called "Calendar"
            'addBGService', // with this action name
            [{                  // and this array of custom arguments to create our entry
                "contactId": contactId
            }]
        ); 
     }
};
module.exports = bgplugin;
