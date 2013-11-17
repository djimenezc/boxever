'use strict';

var myApp = angular.module('currencyApp', ['ngRoute','currencyControllers','currencyDirectives']);

myApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/about', {
		templateUrl : '/assets/partials/about.html',
		controller : 'AboutCtrl'
	}).when('/contact', {
		templateUrl : '/assets/partials/contact.html',
		controller : 'ContactCtrl'
	}).when('/', {
		templateUrl : '/assets/partials/main.html',
		controller : 'CurrencyCtrl'
	}).otherwise({
		redirectTo : '/'
	});
} ]);