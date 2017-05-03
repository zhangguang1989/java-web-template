/**
 * Created by guang.zhang on 2017/4/25.
 */
var app = angular.module("myApp", ['ngRoute']);

app.config(function($routeProvider) {

    $routeProvider.when('/todayInHistory', {
        templateUrl: 'pages/todayInHistory.html',
        controller: 'todayInHistoryCtrl'
    }).when('/', {
        templateUrl: 'pages/hotNews.html',
        controller: 'hotNewsCtrl'
    });

});

app.controller('myCtrl', function($scope, $route, $routeParams, $location) {
    $scope.$route = $route;
    $scope.$location = $location;
    $scope.$routeParams = $routeParams;
});

app.controller('todayInHistoryCtrl', function($scope, $http) {

    $scope.eventList = [];

    $http({
        method: "GET",
        url: "/list/todayInHistory"
    }).then(function (resp) {
        if(resp.data.code == 0){
            $scope.eventList = resp.data.data;
        }else {
            alert(resp.data.msg);
        }
    });

});


app.controller('hotNewsCtrl', function($scope, $http) {

    $scope.eventList = [];

    $http({
        method: "GET",
        url: "/list/hotNews"
    }).then(function (resp) {
        if(resp.data.code == 0){
            $scope.eventList = resp.data.data;
        }else {
            alert(resp.data.msg);
        }
    });

});