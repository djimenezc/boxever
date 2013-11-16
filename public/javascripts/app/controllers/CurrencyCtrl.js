/**
 * 
 */

var currencyApp = angular.module('currencyApp', []);
 
currencyApp.controller('CurrencyCtrl', function ($scope) {
  $scope.currencies = [
    {'name': 'Yen',
     'value': 'JPY'},
    {'name': 'Motorola XOOM™ with Wi-Fi',
     'value': 'The Next, Next Generation tablet.'},
    {'name': 'MOTOROLA XOOM™',
     'value': 'The Next, Next Generation tablet.'}
  ];
  $scope.selectedCurrency = $scope.currencies[0];
});