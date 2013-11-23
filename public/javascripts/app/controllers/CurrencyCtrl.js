'use strict';

currencyControllers.controller('CurrencyCtrl', function ($scope, $http, currencyService) {
	
	$http({method: 'GET', url: '/currency/list'}).
	  success(function(data, status, headers, config) {
		  $scope.currencies = data;
		  $scope.selectedCurrency = $scope.currencies[0];
	  });
	
	$scope.refreshAll = function() {

		console.log('refreshAll');
		
		currencyService.refreshAllExchange();
	};
	
	
	$scope.updateRateData = function() {
		
		var currencyId = $scope.selectedCurrency.value;
		console.log('getRateData ' + currencyId);
		
		currencyService.getCurrencyDataSelected(currencyId);
	};
	
});