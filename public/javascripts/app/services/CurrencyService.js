/**
 * 
 */
var myModule = angular.module('currencyApp.services', []);

myModule.factory('currencyService', function() {
	
	console.log('Currency service starting');
	
	return {
    		
    		getRateData : function(rateId) {
    			
    			console.log('getRateData');
    			
    			$http({method: 'GET', url: '/currency/get/'+rateId}).
    			success(function(data, status, headers, config) {
    				// this callback will be called asynchronously
    				// when the response is available
    				console.log('data: '+ data)
    			});
    		},
    		
    };
    
  });
