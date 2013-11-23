'use strict'

var currencyDirectives = angular.module('currencyDirectives', []);

currencyDirectives.directive('lineChart',['currencyService', '$rootScope', function (currencyService, $rootScope) {
   
	return function (scope, elem, attrs) {
        
		console.log('LineChart init');
		var margin, width, height, currencyRates, x, y, xAxis, yAxis, svg, line, dataStore;
		
		scope.$on('updateChart', function(e, data) {
		    
			console.log('Updating the chart');
			
		    updateChart(data);
		  });

		var resize = function resize() {

			$('#lineChart').empty();

			margin = {
				top : 20,
				right : 20,
				bottom : 30,
				left : 50
			}, width = $('#lineChart').width() - margin.left - margin.right,
					height = 400 - margin.top - margin.bottom;

			x = d3.time.scale().range([ 0, width ]);

			y = d3.scale.linear().range([ height, 0 ]);

			xAxis = d3.svg.axis().scale(x).orient("bottom");

			yAxis = d3.svg.axis().scale(y).orient("left");

			line = d3.svg.line().x(function(d) {
				return x(d.date);
			}).y(function(d) {
				return y(d.close);
			});

			svg = d3.select("#lineChart").append("svg").attr("width",
					width + margin.left + margin.right).attr("height",
					height + margin.top + margin.bottom).append("g").attr(
					"transform",
					"translate(" + margin.left + "," + margin.top + ")");
		};

		var updateChart = function(data) {

			resize();
			
			data = data ? data : currencyRates;
			$rootScope.$broadcast("loading", false);
			if (data) {
				currencyRates = data;
				x.domain(d3.extent(data, function(d) {
					return d.date;
				}));
				y.domain(d3.extent(data, function(d) {
					return d.close;
				}));

				svg.append("g").attr("class", "x axis").attr("transform",
						"translate(0," + height + ")").call(xAxis);

				svg.append("g").attr("class", "y axis").call(yAxis).append("text")
						.attr("transform", "rotate(-90)").attr("y", 6).attr("dy",
								".71em").style("text-anchor", "end").text(
								"Price");

				svg.append("path").datum(data).attr("class", "line")
						.attr("d", line);
			}
			
		};

		var fetchData = function() {

			var ratesData = currencyService.getCurrencyDataSelected();
			
		};

		resize();

		d3.select(window).on('resize', updateChart);

		fetchData();
    };
}]);