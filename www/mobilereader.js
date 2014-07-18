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
			if(!valideCallback(successCallback, errorCallback)) return;
            exec(successCallback, errorCallback, 'MobileReaderPlugin', 'read', options);
        },

        //-------------------------------------------------------------------
        open : function ( successCallback, errorCallback, options) {
			if(!valideCallback(successCallback, errorCallback)) return;

            exec(successCallback, errorCallback, 'MobileReaderPlugin', 'open', options);
        },


        //-------------------------------------------------------------------
        timeout : function (successCallback, errorCallback, options) {
			if(!valideCallback(successCallback, errorCallback)) return;

            exec(successCallback, errorCallback, 'MobileReaderPlugin', 'timeout', options);
        },


        //-------------------------------------------------------------------
        work : function (successCallback, errorCallback, options) {
			if(!valideCallback(successCallback, errorCallback)) return;

            exec(successCallback, errorCallback, 'MobileReaderPlugin', 'work',options);
        },

        //-------------------------------------------------------------------
        lock : function (successCallback, errorCallback, options) {
			if(!valideCallback(successCallback, errorCallback)) return;

            exec(successCallback, errorCallback, 'MobileReaderPlugin', 'lock', options);
        }

}
module.exports = mobilereader;