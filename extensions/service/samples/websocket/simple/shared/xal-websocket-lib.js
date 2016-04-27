// Constructor for an XAL Message Session with the specified remote service
function XAL_MessageSession( servname, host, port ) {
	// message handlers keyed by request ID
	this.responseHandlers = new Object();

	// create a request counter for this instance
	this.createRequestCounter();


	// if the arguments are not supplied, then pick them up from the URL
	var query = window.location.search;
	var query_dict = new Object();
	if ( query != null ) {
		var query_parts = query.substring(1).split( "&" );	// remove leading "?" then split into parts
		var port = null;
		for ( index = 0 ; index < query_parts.length ; index++ ) {
			var pair = query_parts[index].split( "=" );
			var key = pair[0];
			var value = pair[1];
			query_dict[key] = value;
		}
	}

	// if the service name, host or port are specified in the method, use them otherwise look for them in the URL query
	this.service_name = servname != null ? servname : query_dict["servname"];
	this.host = host != null ? host : query_dict["servhost"];
	this.port = port != null ? port : query_dict["servport"];

	// if the host and port are still undefined, then use the localhost and websocket default port as needed
	if ( this.host == null )  this.host = "localhost";
	if ( this.port == null )  this.port = "80";

	var address = "ws://" + this.host + ":" + this.port;

//	console.log( "Service Name: ", this.service_name );
//	console.log( "Address:", address );

	// create the websocket connection
	try {
		this.web_socket = new WebSocket( address );

		this.web_socket.onopen = function ( event ) {
			console.log("Socket opened.");
		};


		this.web_socket.onclose = function ( event ) {
			console.log("Socket closed.");
		};

		// whenever we receive a new message, look for the corresponding response handler, call it if it exists and then delete it
		var responseHandlers = this.responseHandlers;
		this.web_socket.onmessage = function ( event ) {
			var response = JSON.parse( event.data );
			var handler = responseHandlers[response.id];
			if ( handler != null ) {
				handler( response );
				delete responseHandlers[response.id];
			}
		};


		this.web_socket.onerror = function ( event ) {
			console.log( "Socket error:", event );
		};

	} catch (exception) {
		console.log( "Socket exception:", exception);
	}
}


// send a request to the remote service with the arguments as follows: method name, param1, param2, ..., response handler (optional)
XAL_MessageSession.prototype.sendRequest = function() {
	var method = arguments[0];	// first argument is method name

	// if the last argument is a function then it is the response handler otherwise we have no response handler to call
	var has_response_handler = typeof( arguments[arguments.length-1] ) === "function";

	// index of the arguments which marks the upper limit (exclusive) for the last parameter to pass
	var param_end = has_response_handler ? arguments.length - 1 : arguments.length;

	// next arguments are the parameters excluding the last one
	var params = [];
	for ( index = 1 ; index < param_end ; index++ ) {
		params.push( arguments[index] );
	}

	var request = new Object();
	request.message = this.service_name + "#" + method;
	request.params = params;
	request.id = this.nextRequestID();

	if ( has_response_handler ) {
		var responseHandler = arguments[ arguments.length - 1 ];	// response handler is last argument
		this.responseHandlers[request.id] = responseHandler;
		//console.log( "Added response handler with response ID: " + request.id );
	}

	var json_request = JSON.stringify( request );
	this.web_socket.send( json_request );
}


// internally used method to create a method to uniquely identify requests for dispatching the responses to the correct handler
XAL_MessageSession.prototype.createRequestCounter = function() {
	// count of the requests
	var request_counter = 0;

	// increments the request count
	this.nextRequestID = function() {
		return ++request_counter;
	}
}



// below are types for mapping to a XAL specific types (e.g. JavaScript doesn't distinguish between ints and floats so we need to provide conversions)

// generic container for a generic value type
function XAL_Type( type, value ) {
	this.__XALTYPE = type;
	this.value = value;
}



// generic container for an array type (for efficient coding) where each item of the array is of a common type for the entire array
function XAL_ArrayType( itemType, array ) {
	this.__XALITEMTYPE = itemType;
	this.array = array;
}



// adds a generic and array method for conversion to the specified numeric type (e.g. Number.to_xal_int() and Array.to_xal_int_array())
function add_xal_numeric_type( type ) {
	var method = "to_xal_" + type;
	Number.prototype[method] = function() {
		return new XAL_Type( type, this );
	}

	var array_method = "to_xal_" + type + "_array";
	Array.prototype[array_method] = function() {
		return new XAL_ArrayType( type, this );
	}

}

add_xal_numeric_type( "short" );
add_xal_numeric_type( "int" );
add_xal_numeric_type( "long" );
add_xal_numeric_type( "float" );
add_xal_numeric_type( "double" );



// convert an object received from XAL into a suitable JavaScript equivalent
Object.prototype.from_xal = function() {
	if ( this.__XALTYPE ) {		// scalar object
		var type = this.__XALTYPE;

		switch( type ) {
			case "java.lang.Double":
			case "java.lang.Integer":
			case "java.lang.Long":
			case "java.lang.Short":
			case "java.lang.Float":
				return this.value;

			case "java.util.Date":
				var date = new Date();
				date.setTime( this.value );
				return date;

			default:
				return "undefined";
		}
	}
	else if ( this.__XALITEMTYPE ) {	// an array
		return this.array;
	}
	else {		// plain object (e.g. string)
		return this;
	}
}





