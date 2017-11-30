<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>View Chart</title>
        <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
        <script type="text/javascript"  src="http://ajax.googleapis.com/ajax/libs/jquery/1.5/jquery.min.js"></script>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/highcharts.js"></script>
        <script type="text/javascript">
            
            /**
             * NOTE: This code has been taken from Lab10 - highcharts. However, The code has been adapted to display all the lessons and their counts.
             * 
             * Generates AJAX using Jquery
             */
            function requestLessonsCount() {
                // build AJAX request to get lessons and their count, with JSON return
                jQuery.post("${pageContext.request.contextPath}/do/showLessonsOnChart", {}, showLessonOnChart, "json");
                 $('#showChartButton').attr("disabled", true);
            }
              
            /**
             * callback funtion - displays chart with lessons and their counts
             */
            function showLessonOnChart(receivedJSON) {
                console.log(receivedJSON.counts)
                chart = new Highcharts.Chart({
                    chart: {
                        //where this chart should appear 
                        renderTo: 'barChartDiv',
                        //the type of chart we want to see
                        defaultSeriesType: 'column'
                    },
                    title: {
                        text: 'Showing all lessons and the number of times they are booked by users'
                    },
                    xAxis: {
                        // The lessons' name to display on the chart X-axis
                        categories: receivedJSON.names,
                        title: {
                            text: 'Lessons'
                        }
                    },
                    yAxis: {
                        min: 0,
                        max:20,
                        title: {
                            text: 'Number of lessons'
                        }
                    },
                    legend: {
                        layout: 'vertical',
                        backgroundColor: '#FFFFFF',
                        align: 'left',
                        verticalAlign: 'top',
                        x: 100,
                        y: 70,
                        floating: true,
                        shadow: true
                    },
                    tooltip: {
                        formatter: function() {
                            return ''+
                                    this.x +': '+ this.y;
                        }
                    },
                    plotOptions: {
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0
                        }
                    },
                    // The count for each lesson
                    series: [{
                            name: 'Number of lessons booked',
                            data: receivedJSON.counts
                        }]
                });  
            }
            
            /**
             * End of re-use code from Lab10
             */
        </script>
        
    </head>
    <body>
        <h1>Chart view</h1>
        <!-- add the navigation jspx -->
        <%@ include file="/Navigation.jspx" %>
        <!-- tag to display bar chart -->
        <div id="barChartDiv"></div>
        <input id="showChartButton" type='button' onclick="requestLessonsCount()" value="Show chart for lessons"/>
    </body>
</html>
