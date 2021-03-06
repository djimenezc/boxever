'use strict';

var myApp = angular.module('currencyApp', ['ngRoute','currencyControllers','currencyDirectives','currencyServices']);

var currencyServices = angular.module('currencyServices', []);

/**
 * Configure the routes indicating the views depends on the path and the controller that define the scope for
 */
myApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/about', {
		templateUrl : '/assets/partials/about.html',
		controller : 'AboutCtrl'
	}).when('/contact', {
		templateUrl : '/assets/partials/contact.html',
		controller : 'ContactCtrl'
	}).when('/home', {
		templateUrl : '/assets/partials/main.html',
		controller : 'CurrencyCtrl'
	}).otherwise({
		redirectTo : '/home'
	});
} ]);