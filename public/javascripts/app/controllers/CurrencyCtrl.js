'use strict';

currencyControllers.controller('CurrencyCtrl', function ($scope, $http, currencyService) {
	
	$scope.loading = true;

	//Retrieve the list of currencies and load in the scope
	$http({method: 'GET', url: '/currency/list'}).
	  success(function(data, status, headers, config) {
		  $scope.currencies = data;
		  $scope.selectedCurrency = $scope.currencies[0];
	  });
	
	$scope.refreshAll = function() {

		console.log('refreshAll');
		
		$scope.loading = true;
		currencyService.refreshAllExchange();
	};
	
	$scope.$on('loading', function(e, value) {
	    
		$scope.loading = value;
	  });
	
	$scope.cleanDatabase = function() {
		currencyService.cleanDatabase();
	}
	
	$scope.updateRateData = function() {
		
		var currencyId = $scope.selectedCurrency.value;
		console.log('getRateData ' + currencyId);
		
		currencyService.getCurrencyDataSelected(currencyId);
	};
	
});