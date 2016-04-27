// create a new session for sending messages to a service
var message_session = new XAL_MessageSession();

var graph;
var live_update = false;


// send a request to the server to greet
function sendGreetingRequest() {
	// get the input HTML fields
	var recipient_field = document.getElementById( "recipient" );

	var recipient = recipient_field.value;

	// wrap the summand and addend as XAL doubles and send the request to the server
	message_session.sendRequest( "sayHelloTo", recipient, function( response ) {
		var result = response.result;		// get the result
		var greeting_elem = document.getElementById( "greeting" );	// get the output HTML field
		greeting_elem.innerHTML = result.from_xal();
	} );
}


// send a request to the server to get the launch time
function sendLaunchTimeRequest() {
	// wrap the summand and addend as XAL doubles and send the request to the server
	message_session.sendRequest( "getLaunchTime", function( response ) {
		var result = response.result;		// get the result
		var launch_time_elem = document.getElementById( "launchtime" );	// get the output HTML field
		launch_time_elem.innerHTML = result.from_xal();
	} );
}


// send a request to the server to add the summand and addend
function sendAddRequest() {
	// get the input HTML fields
	var summand_field = document.getElementById( "summand" );
	var addend_field = document.getElementById( "addend" );

	// parse the fields for the summand and addend
	var summand = parseFloat( summand_field.value );
	var addend = parseFloat( addend_field.value );

	// test that we have valid numbers
	if ( isNaN( summand ) || isNaN( addend ) ) {
		alert( "Both the summand and addend must be valid numbers." );
		return;
	}

	// wrap the summand and addend as XAL doubles and send the request to the server
	message_session.sendRequest( "add", summand.to_xal_double(), addend.to_xal_double(), function( response ) {
		var sum_elem = document.getElementById( "binarysum" );	// get the output HTML field
		sum_elem.innerHTML = response.result.from_xal();		// display the result
	} );
}


// generate an array of integers to send to the server to sum
function sendSumIntArrayRequest() {
	var summands_elem = document.getElementById( "summandarray" );

	var MAX_COUNT = 20;
	var MAX_INT = 1000;

	// pick a random size for the array
	var count = Math.floor( MAX_COUNT * Math.random() + 1 );

	// generate random integers to populate the array
	var array = new Array();
	for ( index = 0 ; index < count ; index++ ) {
		array.push( Math.floor( 2 * MAX_INT * ( Math.random() - 0.5 ) + 1 ) );
	}
	//window.console.log( array );
	summands_elem.innerHTML = array.toString();

	var int_array = array.to_xal_int_array();

	message_session.sendRequest( "sumIntegers", int_array, function( response ) {
		//console.log( "Completed sum of integers: ", response.result );
		var sum_elem = document.getElementById( "arraysum" );
		sum_elem.innerHTML = response.result.from_xal();
	} );
}


// send a request to the server to generate a sinusoid with the specified parameters
function sendSinusoidGenerateRequest( in_amplitude, in_frequency, in_phase, in_num_points ) {
	if ( in_amplitude == null ) {	// initiated by a button
		live_update = true;
	}

	// get the inputs
	var amplitude = in_amplitude != null ? in_amplitude : parseFloat( document.getElementById( "amplitude" ).value );
	var frequency = in_frequency != null ? in_frequency : parseFloat( document.getElementById( "frequency" ).value );
	var phase = in_phase != null ? in_phase : parseFloat( document.getElementById( "phase" ).value );
	var num_points = in_num_points != null ? in_num_points : parseInt( document.getElementById( "numpoints" ).value );

	// test that we have valid numbers
	if ( isNaN( amplitude ) || isNaN( frequency ) || isNaN( phase ) || isNaN( num_points ) ) {
		alert( "Amplitude, Frequency and Phase must be valid floats and Num Points must be a valid integer." );
		return;
	}

	var processOutput = function( response ) {
		postSinusoidOutput( response );

		if ( live_update ) {
			phase = ( phase + 0.1 ) % 10;			// modulate the phase
			frequency = ( frequency + 0.3 ) % 10;	// modulate the frequency
			setTimeout( function() { sendSinusoidGenerateRequest( amplitude, frequency, phase, num_points ) }, 50 );
		}
	};

	// wrap the summand and addend as XAL doubles and send the request to the server
	message_session.sendRequest( "generateSinusoid", amplitude.to_xal_double(), frequency.to_xal_double(), phase.to_xal_double(), num_points.to_xal_int(), processOutput );
}


// post the sinusoid output
function postSinusoidOutput( response ) {
	var output_elem = document.getElementById( "sinusoid" );	// get the output HTML field

	// remove all content from the output
	var waveform = response.result.from_xal();

	var waveform_points = new XYPlotDataSource();
	for ( windex = 0 ; windex < waveform.length ; windex++ ) {
		waveform_points.addPoint( windex, waveform[windex] );
	}

	graph.removeAllSeries();
	graph.setXLimits( 0, waveform.length );
	graph.addPlotSeries( BAR_CHART, waveform_points, "red" );
	graph.addPlotSeries( LINE_PLOT, waveform_points, "blue" );
	graph.addPlotSeries( SCATTER_PLOT, waveform_points, "yellow" );
	graph.refresh();
}



function init() {
	var sinusoid_output_elem = document.getElementById( "sinusoid" );	// get the output HTML field
	graph = new Graph( 600, 400	);
	sinusoid_output_elem.appendChild( graph.getView() );
	graph.setYLimits( -20.0, 20.0 );
	graph.leftAxis.setLabel( "Signal" );
	graph.bottomAxis.setLabel( "time (seconds)" );
	graph.refresh();
}




