There the code of an html page to show charts of csv file :

```

<html>
  <head>
  <title>-={ Pilogger  :: Day data }=-</title>
    <script src="https://www.google.com/jsapi"></script>
    <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
    <script src="jquery.csv-0.71.js"></script>
    
    <script>
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);

      function drawChart(){
	  	
		drawChartFromCSV("Outside_TemperatureDay.csv" ,'chart_1', "°C");
		drawChartFromCSV("Outside_LightDay.csv" ,'chart_2', "a.u.");
		drawChartFromCSV("Atmospheric_PressureDay.csv" ,'chart_3', "Pa");
		drawChartFromCSV("Backgound_RadiationDay.csv" ,'chart_4', "µSv/h");
		drawChartFromCSV("Room_TemperatureDay.csv" ,'chart_5', "°C");
		drawChartFromCSV("Outside_BatteryDay.csv", 'chart_6', "a.u.");
		drawChartFromCSV("Heating_ExhaustDay.csv" ,'chart_7', "°C");
		drawChartFromCSV("Heating_InflowDay.csv" ,'chart_8', "°C");
		drawChartFromCSV("Heating_ReturnDay.csv" ,'chart_9', "°C");
		drawChartFromCSV("Cellar_temperatureDay.csv" ,'chart_10', "°C");
		drawChartFromCSV("SeismometerDay.csv" ,'chart_11', "a.u.");
		drawChartFromCSV("System_LoadDay.csv" ,'chart_12', "a.u.");
		drawChartFromCSV("System_MemoryDay.csv" ,'chart_13', "Bytes");
		
		setTimeout('drawChart()',120000); 
	  }
	  
	function drawChartFromCSV(filename, elementId, verticalUnit){
	   // grab the CSV
         $.get(filename, function(csvString) {
            // transform the CSV string into a 2-dimensional array
            var arrayData = $.csv.toArrays(csvString, {onParseValue: $.csv.hooks.castToScalar});
        
			var data = new google.visualization.DataTable();
			data.addColumn('datetime', 'Date');
			data.addColumn('number', 'Min');
			data.addColumn('number', 'Average');
			data.addColumn('number', 'Max');
			data.addColumn('number', 'Max');
			
			for (var i = 0; i < arrayData.length-1; i++) {
				var d = new Date();
				d.setTime(arrayData[i+1][0]);
				data.addRow([d, arrayData[i+1][2], arrayData[i+1][1], 
					(arrayData[i+1][3] - arrayData[i+1][2]), arrayData[i+1][3] ]);
			}
			
            var options = {
               title: arrayData[0][1],
               legend: 'none',
			   seriesType: "area",
			   isStacked: true,
	           series: {0: {color: 'transparent'}, 
			   			1: {type: "line"}, 
						2: {color: 'black', lineWidth: 0}, 
						3: {type: "line", color: 'transparent'} },
               vAxis: {viewWindow :{min: data.getColumnRange(1).min, max: data.getColumnRange(4).max}, 
			   		   title: verticalUnit},
			   chartArea: {width: 520, height: 400}
            };
			  
        	var chart = new google.visualization.ComboChart(document.getElementById(elementId));
        	chart.draw(data, options);
       	
	  	});
	  }
	
    </script>
  </head>
  <body>
    <div id="header"  style="width: 750px; height: 80px;">
      <a href="realtime.html">Real time view</a> <a href="hour.html">Hour view</a> <a href="day.html">Day view</a> <a href="month.html">Month view</a> <a href="year.html">Year view</a>
      <h1>Last 24 hours :</h1>
    </div>
    <div id="chart_1" style="width: 750px; height: 500px;"></div>
    <div id="chart_2" style="width: 750px; height: 500px;"></div>
    <div id="chart_3" style="width: 750px; height: 500px;"></div>
    <div id="chart_4" style="width: 750px; height: 500px;"></div>
    <div id="chart_5" style="width: 750px; height: 500px;"></div>
    <div id="chart_6" style="width: 750px; height: 500px;"></div>
    <div id="chart_7" style="width: 750px; height: 500px;"></div>
    <div id="chart_8" style="width: 750px; height: 500px;"></div>
    <div id="chart_9" style="width: 750px; height: 500px;"></div>
    <div id="chart_10" style="width: 750px; height: 500px;"></div>
    <div id="chart_11" style="width: 750px; height: 500px;"></div>
    <div id="chart_12" style="width: 750px; height: 500px;"></div>
    <div id="chart_13" style="width: 750px; height: 500px;"></div>
  </body>
</html>

```