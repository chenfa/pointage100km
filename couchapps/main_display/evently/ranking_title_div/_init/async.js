function(cb) {
    var i = 0;
    var app = $$(this).app;
    var res = [];
    var stop_iterating = false;
    var CONTESTANT_COUNT = 30;

    //error callback
    function callback_error(stat, err, reason) {
	$.log("stat" + JSON.stringify(stat));
	$.log("err" + JSON.stringify(err));
	$.log("reason" + JSON.stringify(reason));
	//alert("Error reading db: " + JSON.stringify(reason));
	stop_iterating = true;
    }

    //success callback
    function read_contestant_info_callback(data) {
	$.log("contestant data: " + JSON.stringify(data)); 

	res.push(data);

	return;
    }

    //read from the db 
    function get_contestant_info(bib) {
	app.db.openDoc('contestant-' + bib, {
	    success: read_contestant_info_callback,
	    error: callback_error
	});
    }

    //load contestants
    for (i = 1; i < CONTESTANT_COUNT; i++) {
	//get_contestant_info(i);
	if (stop_iterating == true)
	    break;

    }

    $.log("gathered data: " + JSON.stringify(res));
    
    function _unwrap_data(data) {
	return data.rows.map(function(row) {
	    return row.value;
	});
    }

    function _get_all_contestants(app, cb1) {
	$.log("here0");
	app.db.view("common/all_contestants", {
	    sucess: function(data) {
		cb1(_unwrap_data(data));
	    }
	});
    }

    $.log("toto");

    function cb2(params) {
	$.log("cb2: " + JSON.stringify(params));
	cb(params);
    }


    _get_all_contestants(app, cb2);

    return;
}

