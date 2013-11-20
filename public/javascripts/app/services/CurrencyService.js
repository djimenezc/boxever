'use strict';

var currencyServices = angular.module('currencyServices', []);

currencyServices
	.factory('currencyService',  function($http) {
	console.log('Currency service starting');
	
	return {
		getCurrencyDataSelected : function(parseDate, callback) {
    			console.log('getRateData');
    			
    			var currencyId = 'USD';
    			
    			$http({method: 'GET', url: 'assets/mockData/data.json'}).
    			success(function(data, status, headers, config) {
    				// this callback will be called asynchronously
    				// when the response is available
    				console.dir('data: '+ data)
    				
    				data.forEach(function(d) {
    					d.date = parseDate(d.name);
    					d.close = +d.value;
    				});

    				callback(data);    		
    			});
    		}
    		
    };
    
  });
