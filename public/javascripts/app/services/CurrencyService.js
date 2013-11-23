'use strict';

currencyServices
	.factory('currencyService', ['$http', '$rootScope', function($http, $rootScope) {
	console.log('Currency service starting');
	
	var dateParser = function(date) {

		return d3.time.format("%d-%b-%y").parse(date);
	}
	
	return {
		getCurrencyDataSelected : function(currencyId) {
    			console.log('getRateData');
    			
    			var currencyId = currencyId ? currencyId : 'USD';
    			
    			$http({method: 'GET', url: '/currency/get/'+currencyId}).
    			success(function(data, status, headers, config) {

    				console.dir('Retrieved currency data successfully!!!! ');
    				
    				data.forEach(function(d) {
    					d.date = dateParser(d.name);
    					d.close = +d.value;
    				});

    				$rootScope.$broadcast("updateChart", data);
    			});
    		},
    		
    		refreshAllExchange : function() {
    			$http({method: 'GET', url: '/currency/refreshAll'}).
    			  success(function(data, status, headers, config) {

    				  console.log('Result refresh all call: ' + data.name);
    			  });
    		}
    		
    };
    
  }]);
