'use strict';

currencyServices
	.factory('currencyService', ['$http', '$rootScope', function($http, $rootScope) {
	console.log('Currency service starting');
	
	var dateParser = function(date) {

		return d3.time.format("%d-%b-%y").parse(date);
	}
	
	return {
		//Get the exchange rates for 
		getCurrencyDataSelected : function(currencyId) {
    			console.log('getRateData');
    			
    			var currencyId = currencyId ? currencyId : defaultCurrency;
    			
    			$rootScope.$broadcast("loading", true);
    			
    			$http({method: 'GET', url: '/currency/get/'+currencyId}).
    			success(function(data, status, headers, config) {

    				console.dir('Retrieved currency data successfully!!!! ');
    				
    				if(data) {
    					data.forEach(function(d) {
    						d.date = dateParser(d.name);
    						d.close = +d.value;
    					});
    					
    					$rootScope.$broadcast("updateChart", data);
    					$rootScope.$broadcast("loading", false);
    				}
    			});
    		},
    		
    		refreshAllExchange : function() {
    			
    			$rootScope.$broadcast("loading", true);
    			
    			$http({method: 'GET', url: '/currency/refreshAll'}).
    			  success(function(data, status, headers, config) {

    				  console.log('Result refresh all call: ' + data.name);
    				  $rootScope.$broadcast("loading", false);
    			  });
    		},
    		
    		cleanDatabase: function() {
    			
    			$rootScope.$broadcast("loading", true);
    			
    			$http({method: 'GET', url: '/currency/cleanDatabase'}).
    			success(function(data, status, headers, config) {
    				
    				console.log('Database clean status: ' + data);
    				$rootScope.$broadcast("loading", false);
    			});
    		}
	}
  }]);
