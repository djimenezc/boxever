'use strict';

var currencyControllers = angular.module('currencyControllers', []);

currencyControllers.controller('GlobalCtrl', function ($scope, $http,$location) {
	
	console.log('Contact init');
	
	//Function to activate the element active in the header tabs 
	$scope.getClass = function(path) {
	    if ($location.path().substr(0, path.length) == path) {
	      return "active"
	    } else {
	      return ""
	    }
	}
});

