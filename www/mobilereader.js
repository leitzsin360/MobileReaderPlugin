var mobilereader =  {
		valideCallback:function(successCallback, errorCallback){
            if (errorCallback == null) {
                errorCallback = function () {
                };
            }

            if (typeof errorCallback != "function") {
                console.log("errorCallback failure: failure parameter not a function");
                return false;
            }

            if (typeof successCallback != "function") {
                console.log("successCallback.scan failure: success callback parameter must be a function");
                return false;
            }
            return true;
		},
        /**
         * Read code from reader.
         */
        read : function (successCallback, errorCallback,options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;
            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'read', options);
        },

        //-------------------------------------------------------------------
        standby : function ( successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'standby', options);
        },

        //-------------------------------------------------------------------
        exit : function ( successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'exit', options);
        },


        //-------------------------------------------------------------------
        open : function ( successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'open', options);
        },


        //-------------------------------------------------------------------
        timeout : function (successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'timeout', options);
        },


        //-------------------------------------------------------------------
        worklock : function (successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'worklock',options);
        },

        //-------------------------------------------------------------------
        reset : function (successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'reset', options);
        },
		
        //-------------------------------------------------------------------
        test : function (successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'test', options);
        },		
		
        //-------------------------------------------------------------------
        writekey : function (successCallback, errorCallback, options) {
			if(!this.valideCallback(successCallback, errorCallback)) return;

            cordova.exec(successCallback, errorCallback, 'MobileReaderPlugin', 'writekey', options);
        }		

};
console.log("mobilereader inited");

module.exports = mobilereader;