'use strict';

var myApp = angular.module('currencyApp', ['ngRoute','currencyControllers','currencyDirectives','currencyServices']);

var currencyServices = angular.module('currencyServices', []);

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