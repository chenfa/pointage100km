function(cb, wtf, request) {
  var app = $$(this).app;

  var request_int = parseInt(request);
  if (!isNaN(request_int)) {
    app.db.openDoc(infos_id(request_int), {
      success: function(data) {
        cb([{value: data}]);
      },
      error: function() {
        cb([]);
      }
    });
  } else {
    db_search_str(app, request, function(data) {
      var res = data.rows;
      res.request = request;
      cb(res);
    });
  }
};
