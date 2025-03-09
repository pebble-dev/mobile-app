/** @namespace Pebble */
// important note for this script: it expects that the native Pebble object has been
// added to the webview (via addJavascriptInterface) *before* it runs
//
// as of Android 4.4, this should be 'automatic' since the WebView source code serially executes all calls
// into it on its own thread.  If this ever changes, it is worth revisiting this.

/**** private calls ***/
_appMessageAckCallbacks = {};
_appMessageNackCallbacks = {};

// Override XMLHttpRequest

XMLHttpRequest.prototype.uniqueID = function( ) {
    if (!this.uniqueIDMemo) {
        this.uniqueIDMemo = Math.floor(Math.random( ) * 1000);
    }
    return this.uniqueIDMemo;
}

var sDefaultTimeout = 30000;

XMLHttpRequest.prototype.oldSend = XMLHttpRequest.prototype.send;

var newSend = function(a) {
    var xhr = this;
    _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted send (" + a + ") timeout = " + xhr.timeout
        + " hasOnTimeout = " + (undefined != xhr.ontimeout) + " async = " + xhr.pebbleAsync);
    _Pebble.logInterceptedSend();
    if (0 == xhr.timeout) {
        if (!xhr.pebbleAsync) {
            _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted; not setting timeout for synchronous request");
        } else {
            _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted; setting missing timeout to " + sDefaultTimeout);
            xhr.timeout = sDefaultTimeout;
        }
    }

    var onload = function( ) {
        _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted load: " + xhr.status);
    };
    var onerror = function( ) {
        _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted error: " +	xhr.status);
    };
    var ontimeout = function( ) {
        _Pebble.privateLog("[" + xhr.uniqueID( ) + "] intercepted timeout: " + xhr.status);
    };

    xhr.addEventListener("load", onload, false);
    xhr.addEventListener("error", onerror, false);
    xhr.addEventListener("timeout", ontimeout, false);

    xhr.oldSend(a);
}

XMLHttpRequest.prototype.send = newSend;

XMLHttpRequest.prototype.oldOpen = XMLHttpRequest.prototype.open;

var newOpen = function(method, url, async, user, password) {
    _Pebble.privateLog("[" + this.uniqueID( ) + "] intercepted open (" + method + " , " + url + " , async = " + async + ")");
    // When async parameter value is omitted, use true as default
    if (arguments.length < 3 || undefined == async) {
        async = true;
    }
    // Store the async parameter in our own member
    this.pebbleAsync = async;

    // Pass the parameters through to super() according to how thehy were presented to us
    if (arguments.length > 4) {
        this.oldOpen(method, url, async, user, password);
    } else if (arguments.length > 3) {
        this.oldOpen(method, url, async, user);
    } else {
        this.oldOpen(method, url, async);
    }
}

XMLHttpRequest.prototype.open = newOpen;

// End overriding XMLHttpRequest

// Override location request
navigator.geolocation.__proto__.originalGetCurrentPosition = navigator.geolocation.getCurrentPosition;
var overridenGetCurrentPosition = function(locationSuccess, locationError, locationOptions) {
    _Pebble.logLocationRequest();
    navigator.geolocation.originalGetCurrentPosition(locationSuccess, locationError, locationOptions);
}
navigator.geolocation.__proto__.getCurrentPosition = overridenGetCurrentPosition;
// End override location request

function isFunction(functionToCheck) {
    if ((functionToCheck == null) || (functionToCheck == undefined)) {
        return false;
    }
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
}

var PebbleEventListener = {
    events: [],

    addEventListener: function( type, callback, useCapture ) {

        if( !this.events[type] ) {
            this.events[type] = [];

            // call the event initializer, if this is the first time we
            // bind to this event.
            if( typeof(this._eventInitializers[type]) == 'function' ) {
                this._eventInitializers[type]();
            }
        }

        if (isFunction(callback)) {
            this.events[type].push( callback );
        }
    },

    removeEventListener: function( type, callback ) {
        var listeners = this.events[ type ];
        if( !listeners ) { return; }

        for( var i = listeners.length; i--; ) {
            if( listeners[i] === callback ) {
                listeners.splice(i, 1);
            }
        }
    },

    _eventInitializers: {},
    dispatchEvent: function( event ) {

        var listeners = this.events[ event.type ];
        if( !listeners ) { return false; }

        // fire off a duplicate listener array, in case any of them add new events
        listeners = listeners.slice(0);
        var removeList = [];
        var returnVal = true;
        for( var i = 0; i < listeners.length; i++ ) {
            try {
                var removeListener = listeners[i]( event );
                if (removeListener === true) {
                    removeList.push(i);
                }
            }
            catch (e) {
                //guard against bad external code calls
                console.log('jskit_system :: PebbleEventListener : bad dispatch on event '+event.type + ': ' + e);
                returnVal = false;
            }
        }
        for (var i = (removeList.length)-1; i >= 0;i--) {
            try {
                listeners.splice(removeList[i],1);
                console.log('jskit_system :: PebbleEventListener : post-dispatch removed listener ' + removeList[i]
                + ' on event '+ event.type);
            }
            catch (e) {
                console.log('jskit_system :: PebbleEventListener : post-dispatch failed to remove listener '
                + removeList[i]
                + ' on event '+ event.type);
            }
        }
        return returnVal;
    }
}

function signalWebviewOpenedEvent(data) {
    var event = document.createEvent('Event');
    event.initEvent('webviewopened',true,true);
    event.type = 'webviewopened';
    event.data = data;
    event.opened = data;
    PebbleEventListener.dispatchEvent(event);
}

function signalReady(data) {
    // _initLocalStorageMonitor();
    var event = document.createEvent('Event');
    event.initEvent('ready',true,true);
    event.type = 'ready';
    event.data = data;
    event.ready = data;
    var success = PebbleEventListener.dispatchEvent(event);
    //callback into Pebble. (jskit) to
    // confirm this app is now armed n' ready in all respects
    // is able to execute JS code.
    // this doesn't guarantee that the 3rd party JS will continue to run
    // as it may be itself broken/have errors.
    // however, getting here means that the bootstrap and loading has succeeded

    try {
        _Pebble.privateFnConfirmReadySignal(success);
        //start a heartbeat timer
        setInterval(function(){
            try {
                _Pebble.privateFnHeartbeatPeriodic();
            }
            catch(exc) {}
        },5000);
    }
    catch (ex) {
    }
}

function signalWebviewClosedEvent(data) {
    var event = document.createEvent('Event');
    event.initEvent('webviewclosed',true,true);
    event.type = 'webviewclosed';
    try {
        var decodedData;

        if (data && data.length > 0) {
            decodedData = decodeURIComponent(data);
        }
        event.data = decodedData;
        event.response = decodedData;
    }
    catch (e) {
        event.data = data;
        event.response = data;
    }
    PebbleEventListener.dispatchEvent(event);
}

function signalNewAppMessageData(data) {
    var event = document.createEvent('Event');
    event.initEvent('appmessage',true,true);
    event.type = 'appmessage';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }
    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);
}

function signalAppMessageAck(data) {
    var event = document.createEvent('Event');
    event.initEvent('appmessage_ack',true,true);
    event.type = 'appmessage_ack';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }

    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);

    if (event.payload.transactionId != undefined) {
        removeAppMessageCallbacksForTransactionId(event.payload.transactionId);
    }
}

function signalAppMessageNack(data) {
    var event = document.createEvent('Event');
    event.initEvent('appmessage_nack',true,true);
    event.type = 'appmessage_nack';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }

    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);

    if (event.payload.transactionId != undefined) {
        removeAppMessageCallbacksForTransactionId(event.payload.transactionId);
    }
}

function removeAppMessageCallbacksForTransactionId(tid) {
    if (_appMessageAckCallbacks[tid]) {
        PebbleEventListener.removeEventListener('appmessage_ack',_appMessageAckCallbacks[tid]);
    }

    if (_appMessageNackCallbacks[tid]) {
        PebbleEventListener.removeEventListener('appmessage_nack',_appMessageNackCallbacks[tid]);
    }

    _appMessageAckCallbacks[tid] = undefined;
    _appMessageNackCallbacks[tid] = undefined;
}

function signalSettingsWebuiLaunchOpportunity(data) {

    var event = document.createEvent('Event');
    event.initEvent('showConfiguration',true,true);
    event.type = 'showConfiguration';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }
    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);

    //it was called something else before so i am maintaing that
    //don't ask me to remove it, at least for the next 3-4 weeks

    var earlyVersionCompatEvent = document.createEvent('Event');
    earlyVersionCompatEvent.initEvent('settings_webui_allowed',true,true);
    earlyVersionCompatEvent.type = 'settings_webui_allowed';
    try {
        earlyVersionCompatEvent.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        earlyVersionCompatEvent.payload = {};
    }
    earlyVersionCompatEvent.data = earlyVersionCompatEvent.payload;
    PebbleEventListener.dispatchEvent(earlyVersionCompatEvent);
}

function _initLocalStorageMonitor(usedPriorToScriptLoadComplete,loadingUrl) {
    localStorage.clear();
    var storageItemsUnparsed = {};
    if (usedPriorToScriptLoadComplete == true) {
        storageItemsUnparsed =
            _Pebble.privateFnLocalStorageReadAll_AtPreregistrationStage(loadingUrl);
    }
    else {
        storageItemsUnparsed =
            _Pebble.privateFnLocalStorageReadAll();
    }

    var storageItems;
    if (storageItemsUnparsed) {
        storageItems = JSON.parse(storageItemsUnparsed);
    }

    var storageKeys = Object.keys(storageItems);
    storageKeys.forEach(function(key, idx, keys) {
        localStorage[key] = storageItems[key];
    });

    var iframe = document.createElement("iframe");
    document.documentElement.appendChild(iframe);

    iframe.contentWindow.addEventListener("storage", function(event) {
        if (!event.key) {
            return;
        }

        if (event.storageArea != iframe.contentWindow.localStorage) {
            console.log("storage event not fired on localstorage. ignoring.");
            return;
        }

        if (event.newValue === null) {
            _Pebble.privateFnLocalStorageRemove(event.key);
        } else {
            _Pebble.privateFnLocalStorageWrite(event.key, event.newValue);
        }
    }, false);
}

function signalLoaded() {
    console.log("signalLoaded");
    try {
        console.log("inside try-bridge-active");
    }
    catch (e) {
        console.log("signalLoaded : bridge not yet ready...retry-delay");
        setTimeout(function(){signalLoaded()},100);
        return;
    }

    _Pebble.signalAppScriptLoadedByBootstrap();
    console.log("signalLoaded (finalized)");
}

function signalBodyLoaded() {
    console.log("signalBodyLoaded");
}

function loadScript(url)
{
    console.log("loadScript "+url);
    // adding the script tag to the head
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.charset = "UTF-8"

    // then bind the event to the callback function
    // there are several events for cross browser compatibility
    script.onreadystatechange = signalLoaded;
    script.onload = signalLoaded;

    // LOCALSTORE-BEFORE-READY-EVENT HXMOD
    _initLocalStorageMonitor(true,url);

    // fire the loading
    head.appendChild(script);
}

function loadBody(url)
{
    console.log("loadBody "+url);
    var body = document.getElementsByTagName('body')[0];
    var newbody = document.createElement('body');

    newbody.onreadystatechange = signalBodyLoaded;
    newbody.onload = signalBodyLoaded;
    newbody.src = url;
    //fire the loading
    console.log("document replacechild");
}

function httpGetNativeJsSynchro(theUrl)
{
    var xmlHttp = null;

    xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false );
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

function scriptHasEmbeddedAppInfo() {
    return (appinfo && (typeof appinfo.info != 'undefined'));
}

function pingWebcontext(opt) {
    if (opt != undefined) {
        _Pebble.pong(opt);
    }
    else {
        _Pebble.pong("(no-opt)");
    }
}

var getTimelineSubscribeToTopicURL = function(topic) {
    // TODO: Get and use from future boot.json URL template
    var encodedTopic = encodeURIComponent(topic);
    return "https://timeline-api.getpebble.com/v1/user/subscriptions/" + encodedTopic;
};

var getTimelineSubscriptionsListURL = function() {
    // TODO: Get and use from future boot.json URL template
    return "https://timeline-api.getpebble.com/v1/user/subscriptions";
};

/**** public calls ***/

/**
 * Adds a listener for Pebble JS events.
 *
 * Valid event types:
 *
 * appmessage: watch sent an AppMessage to JS. AppMessage is contained in the payload property of the event object.
 * it consists of key-value pairs.  the keys are strings containing integers, or aliases for keys defined in appinfo.json.
 * values will be numbers, or arrays of characters
 *
 * showConfiguration: the user has requested that a configuration view be loaded.  this could be caused by an
 * initial app install, or by the user tapping the configuration button in the Pebble phone app
 *
 * webviewclosed: an open webview was closed by the user.  if the webview had a response, it will be contained in
 * the response property
 *
 * @function addEventListener
 * @memberof Pebble
 * @param type event type
 * @param callback function to receive event
 * @param useCapture true if events should be captured
 */
Pebble.addEventListener = function(type, callback, useCapture) {
    PebbleEventListener.addEventListener(type, callback, useCapture);
};

/**
 * remove an existing event listener
 * @function removeEventListener
 * @memberof Pebble
 * @param type type of event
 * @param callback existing registered callback
 */
Pebble.removeEventListener = function(type, callback) {
    PebbleEventListener.removeEventListener(type, callback);
};

/**
 * send an AppMessage to app running on Pebble
 * @function sendAppMessage
 * @memberof Pebble
 * @param jsonAppMessage an object containing key-value pairs to send to the watch.  keys must be strings containing
 * integers, or aliases defined in appinfo.json.  values must be integers, arrays of bytes, or strings.  values
 * in arrays greater than 255 will be mod 255 before sending.
 * @param callbackForAck callback to run when watch sends ack on appmessage
 * @param callbackForNack callback to run when watch sends nack on appmessage, or sending failed
 * @returns transaction id
 */
Pebble.sendAppMessage = function(rawJsonObjectToSend,callbackForAck,callbackForNack) {
    var transactionId = null;
    try {
        transactionId = _Pebble.sendAppMessageString(JSON.stringify(rawJsonObjectToSend));
    }
    catch (e) {
        console.log('misuse of sendAppMessage(raw JSON object to send)...check that parameter');
    }

    if (transactionId == null) {
        throw "Error sending app message. Unknown key.";
    }


    if (callbackForAck != undefined) {
        var wrappedCallbackforAck = function(e) {
            try {
                if (e.data.transactionId == transactionId) {
                    //                    console.log("calling Ack callback for transactionID: " + transactionId);
                    callbackForAck(e);
                } else {
                    //                    console.log("ack fakeout");
                }
            }
            catch (exx) {}

        }
        _appMessageAckCallbacks[transactionId] = wrappedCallbackforAck;

        try {
            PebbleEventListener.addEventListener('appmessage_ack',wrappedCallbackforAck);
        }
        catch (e) {
            console.log('misuse of sendAppMessage(raw JSON object to send): ack callback param is bad');
        }
    }

    if (callbackForNack != undefined) {
        var wrappedCallbackforNack = function(e) {
            try {
                if (e.data.transactionId == transactionId) {
                    //                    console.log("calling Nack callback");
                    callbackForNack(e);
                } else {
                    //                    console.log("Nack fakeout");
                }
            }
            catch (exx) {}
        }
        _appMessageNackCallbacks[transactionId] = wrappedCallbackforNack;
        try {
            PebbleEventListener.addEventListener('appmessage_nack',wrappedCallbackforNack);
        }
        catch (e) {
            console.log('misuse of sendAppMessage(raw JSON object to send): nack callback param is bad');
        }
    }

    return transactionId;
}

function signalTimelineTokenSuccess(data) {
    var event = document.createEvent('Event');

    event.initEvent('getTimelineTokenSuccess',true,true);
    event.type = 'getTimelineTokenSuccess';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }
    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);
}

function signalTimelineTokenFailure(data) {
	var event = document.createEvent('Event');

    event.initEvent('getTimelineTokenFailure',true,true);
    event.type = 'getTimelineTokenFailure';
    try {
        event.payload = JSON.parse(data);
    }
    catch (e) {
        console.log('failed to JSON.parse data passed in');
        event.payload = {};
    }
    event.data = event.payload;
    PebbleEventListener.dispatchEvent(event);
}

Pebble.getTimelineToken = function(successCallback, failureCallback) {
	try {
		instanceCallId = _Pebble.getTimelineTokenAsync();

		var successWrapper = function(e) {
			callId = e.data.callId;
			token = e.data.userToken;
			if (callId == instanceCallId) {
				successCallback(token);
				PebbleEventListener.removeEventListener('getTimelineTokenSuccess', successWrapper);
				PebbleEventListener.removeEventListener('getTimelineTokenFailure', failureWrapper);
			}
		};

		var failureWrapper = function(e) {
		    callId = e.data.callId;
		    if (callId == instanceCallId) {
			    failureCallback();
			    PebbleEventListener.removeEventListener('getTimelineTokenSuccess', successWrapper);
			    PebbleEventListener.removeEventListener('getTimelineTokenFailure', failureWrapper);
			}
		};

		PebbleEventListener.addEventListener('getTimelineTokenSuccess', successWrapper);
		PebbleEventListener.addEventListener('getTimelineTokenFailure', failureWrapper);
	} catch (e) {
        console.log('Error in the getTimelineToken method');
	}
}

Pebble.timelineSubscribe = function(topic, successCb, errorCb) {
    window.Pebble.getTimelineToken(
        function(token) {
            var url = getTimelineSubscribeToTopicURL(topic);
            var xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
            xhr.onload = function (e) {
              if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                  successCb();
                } else {
                  errorCb();
                }
              }
            };
            xhr.onerror = function (e) {
              errorCb();
            };
            xhr.timeout = 15000;
            xhr.setRequestHeader ("X-User-Token", token);
            xhr.send(null);
        },
        errorCb
    );
};

Pebble.timelineUnsubscribe = function(topic, successCb, errorCb) {
    window.Pebble.getTimelineToken(
        function(token) {
            var url = getTimelineSubscribeToTopicURL(topic);
            var xhr = new XMLHttpRequest();
            xhr.open("DELETE", url, true);
            xhr.onload = function (e) {
              if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                  successCb();
                } else {
                  errorCb();
                }
              }
            };
            xhr.onerror = function (e) {
              errorCb();
            };
            xhr.timeout = 15000;
            xhr.setRequestHeader ("X-User-Token", token);
            xhr.send(null);
        },
        errorCb
    );
};

Pebble.timelineSubscriptions = function(successCb, errorCb) {
    window.Pebble.getTimelineToken(
        function(token) {
            var url = getTimelineSubscriptionsListURL();
            var xhr = new XMLHttpRequest();
            xhr.open("GET", url, true);
            xhr.onload = function (e) {
              if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                  try {
                    var responseJSON = JSON.parse(xhr.responseText);
                    successCb(responseJSON["topics"]);
                  } catch (err) {
                    errorCb("Malformed response from server: " + err);
                  }
                } else {
                  errorCb("Error loading from server");
                }
              }
            };
            xhr.onerror = function (e) {
              errorCb(e);
            };
            xhr.timeout = 15000;
            xhr.setRequestHeader ("X-User-Token", token);
            xhr.send(null);
        },
        function () {
          errorCb("Error getting token");
        }
    );
};

Pebble.getActiveWatchInfo = function() {
    var data = _Pebble.getActivePebbleWatchInfo();
    if (data === "") {
        return null;
    } else {
        return JSON.parse(data);
    }
}

Pebble.appGlanceReload = function(appGlanceSlices, appGlanceReloadSuccessCallback, appGlanceReloadFailureCallback) {
    var success = _Pebble.reloadAppGlances(JSON.stringify(appGlanceSlices));
    var callbackPayload = {"success": success};
    if (success === true) {
        appGlanceReloadSuccessCallback(appGlanceSlices, callbackPayload);
    } else {
        appGlanceReloadFailureCallback(appGlanceSlices, callbackPayload);
    }
}

window.cobble = true;