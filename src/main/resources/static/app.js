/**
 * Created by guang.zhang on 2017/4/25.
 */
var app = angular.module("myApp", []);
app.controller("myCtrl", function($scope, $http) {

    $scope.eventList = [];

    $http({
        method: "GET",
        url: "/todayInHistory"
    }).then(function (resp) {
        if(resp.data.code == 0){
            $scope.eventList = resp.data.data;
        }else {
            alert(resp.data.msg);
        }
    });

});
