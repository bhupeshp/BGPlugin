var bgplugin = {
        createEvent: function(accountName, accountPwd, serverURL) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'BGPlugin', // mapped to our native Java class called "Calendar"
            'addBGService', // with this action name
            [{                  // and this array of custom arguments to create our entry
                "accountName": accountName,
                "accountPwd": accountPwd,
                "serverURL": serverURL
            }]
        ); 
     }
};
module.exports = bgplugin;
