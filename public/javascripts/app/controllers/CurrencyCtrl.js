'use strict';

var currencyControllers = angular.module('currencyControllers', []);

currencyControllers.controller('CurrencyCtrl', function ($scope, $http) {
	
	$http({method: 'GET', url: '/currency/list'}).
	  success(function(data, status, headers, config) {
		  $scope.currencies = data;
		  $scope.selectedCurrency = $scope.currencies[0];
	  });
	
	$scope.refreshAll = function() {

		console.log('refreshAll');
		
		$http({method: 'GET', url: '/currency/refreshAll'}).
		  success(function(data, status, headers, config) {
		    // this callback will be called asynchronously
		    // when the response is available
			  console.log('Result refresh all call:' + data);
		  });
	};
	
	
	$scope.getRateData = function() {
		
		console.log('getRateData');
		var rateId = $scope.value;
		
		$http({method: 'GET', url: '/currency/get/'+rateId}).
		success(function(data, status, headers, config) {
			// this callback will be called asynchronously
			// when the response is available
			console.log('data: '+ data)
		});
	};
	
});