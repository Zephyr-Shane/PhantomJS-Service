(function()
{
    var system = require('system');
    var fs = require('fs');
    var config = {
        // define the location of js files
        //JQUERY : '../js/jquery.js',
		JQUERY : 'echarts/jquery-1.9.1.min.js',  
        //ECHARTS3 : '../../thirdlib/layuiadmin/scripts/echarts-4.4.0-rc.1.js',
        ECHARTS3 : 'echarts/echarts-4.4.0-rc.1.js',
        // ECHARTS3 : 'echarts.min_4.js',
        // default container width and height
        DEFAULT_WIDTH : '1000',
        DEFAULT_HEIGHT : '600'
    }, parseParams, render;

    usage = function(m)
    {
        console.log( m + "Usage: phantomjs echarts-convert.js -txtPath path1 -picTmpPath path2 -picPath path3 -width width -height height\n");
    };

    errExist = function()
    {
        console.log("exist error, will do phantom exist...");
        phantom.exit();
    }

    pick = function()
    {
        try
        {
            var args = arguments, i, arg, length = args.length;
            for (i = 0; i < length; i += 1)
            {
                arg = args[i];
                if (arg !== undefined && arg !== null && arg !== 'null' && arg != '0')
                {
                    return arg;
                }
            }
        } catch (e)
        {
            console.log(" Err :" + e);
            errExist();
        }
    };
    var base64Pad = '=';
    var toBinaryTable = [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
            54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 0, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
    ];
    base64ToString = function(data)
    {
        // console.log("data = "+data);
        var result = '';
        try
        {
            var leftbits = 0; // number of bits decoded, but yet to be appended
            var leftdata = 0; // bits decoded, but yet to be appended
            // Convert one by one.
            for (var i = 0; i < data.length; i++)
            {
                var c = toBinaryTable[data.charCodeAt(i) & 0x7f];
                // console.log("i = "+c);
                var padding = (data.charCodeAt(i) == base64Pad.charCodeAt(0));
                // Skip illegal characters and whitespace
                if (c == -1)
                    continue;

                // Collect data into leftdata, update bitcount
                leftdata = (leftdata << 6) | c;
                leftbits += 6;

                // If we have 8 or more bits, append 8 bits to the result
                if (leftbits >= 8)
                {
                    leftbits -= 8;
                    // Append if not padding.
                    if (!padding)
                        result += String.fromCharCode((leftdata >> leftbits) & 0xff);
                    leftdata &= (1 << leftbits) - 1;
                }
            }

            console.log(result);
            // if (leftbits)
            // throw Components.Exception('Corrupted base64 string');
        } catch (e)
        {
            console.log("base64ToString() Err :" + e);
            errExist();
        }

        return result;
    };

    getParams = function()
    {
        var map = {}, i, key;
        if (system.args.length < 2)
        {
            usage("getParams(): system.args.length < 2");
            phantom.exit();
        }
        for (i = 0; i < system.args.length; i += 1)
        {
            if (system.args[i].charAt(0) === '-')
            {
                key = system.args[i].substr(1, i.length);
                try
                {
                    var val = system.args[i + 1];
                    console.log("getParams(): " + key + "=" + val);
                    map[key] = val;
                } catch (e)
                {
                    console.log("getParams() Error:" + system.args[i + 1]);
                    console.log(e);
                    errExist();
                }
            }
        }
        return map;
    };

    parseParams = function()
    {
        var map = {};
        try
        {
            var data = getParams();
            for ( var key in data)
            {
                map[key] = data[key].split(",");
            }

            var op = [];
            for (var j = 0; j < map['txtPath'].length; j++)
            {
                var str = fs.read(map['txtPath'][j]);
                op[j] = str;
            }
            map['option'] = op;

            if (!(map['option']) || map['option'] === undefined || map['option'].length === 0)
            {
                usage("parseParams() ERROR: No option or infile found.");
                errExist();
            }

            if (!(map['option'].length == map['picTmpPath'].length && map['picTmpPath'].length == map['picPath'].length))
            {
                usage("parseParams() ERROR: params error：params’s length is not same.");
                errExist();
            }
        } catch (e)
        {
            console.log("parseParams() Err :" + e);
            errExist();
        }
        return map;
    };

    render = function(params)
    {
        try
        {
            var page = require('webpage').create(), createChart;

            page.onConsoleMessage = function(msg)
            {
                console.log("Console:【" + msg + "】");
            };

            page.onAlert = function(msg)
            {
                console.log("Alert:【" + msg + "】");
            };

            createChart = function(inputOption, width, height)
            {
                var counter = 0;
                function decrementImgCounter()
                {
                    counter -= 1;
                    if (counter < 1)
                    {
                        console.log("decrementImgCounter() : " + messages.imagesLoaded);
                    }
                }

                function loadScript(varStr, codeStr)
                {
                    try
                    {
                        var script = $('<script>').attr('type', 'text/javascript');
                        // script.html('var ' + varStr + ' = ' + codeStr);
                        script.html(codeStr);
                        document.getElementsByTagName("head")[0].appendChild(script[0]);
                        if (window[varStr] !== undefined)
                        {
                            console.log('Echarts.' + varStr + ' has been parsed');
                        }
                    } catch (e)
                    {
                        console.log("loadScript() Err :" + e);
                        errExist();
                    }
                }

                function loadImages()
                {
                    try
                    {
                        var images = $('image'), i, img;
                        if (images.length > 0)
                        {
                            counter = images.length;
                            for (i = 0; i < images.length; i += 1)
                            {
                                img = new Image();
                                img.onload = img.onerror = decrementImgCounter;
                                img.src = images[i].getAttribute('href');
                            }
                        } else
                        {
                            console.log('loadImages() : The images have been loaded complete');
                        }
                    } catch (e)
                    {
                        console.log("loadImages() Err :" + e);
                        errExist();
                    }
                }

                // load opiton
                if (inputOption != 'undefined')
                {
                    // parse the option
                    loadScript('option', inputOption);
                    // disable the animation
                    option.animation = false;
                }

                // we render the image, so we need set background to white.
                $(document.body).css('backgroundColor', 'white');
                var container = document.getElementById('container');
                if(!container)
                {
                    container = $("<div>").appendTo(document.body);
                    container.attr('id', 'container');
                    container.css({
                        width : width,
                        height : height
                    });
                }

                // render the chart  
                var myChart = echarts.getInstanceByDom(document.getElementById("container"));
                if(!myChart)
                {
                    // 防止重复初始化
                    myChart = echarts.init(document.getElementById("container"));
                }
                myChart.clear();
                myChart.setOption(option);
                // load images  
                loadImages();
            };

            // parse the params  
            page.open("about:blank", function(status)
            {
                console.log("------------start page.open------------")
                // inject the dependency js  
                page.injectJs(config.JQUERY);
                page.injectJs(config.ECHARTS3);

                for (var i = 0; i < params['option'].length; i++)
                {
                    var width = params['width'] ? params['width'][i] : config.DEFAULT_WIDTH;
                    var height = params['height'] ? params['height'][i] : config.DEFAULT_HEIGHT;

                    // define the clip-rectangle  
                    page.clipRect = {
                        top : 0,
                        left : 0,
                        width : width,
                        height : height
                    };

                    // create the chart  
                    page.evaluate(createChart, params['option'][i], width, height);
                    // render the image
                    var rederFlag = page.render(params['picTmpPath'][i]);
                    if (rederFlag === true)
                    {
                        console.log(i + ':render pic to picTmpPath complete: picTmpPath=' + params['picTmpPath'][i]);
                        try
                        {

                            if (fs.exists(params['picPath'][i]))
                            {
                                fs.remove(params['picPath'][i]);
                            }
                            //fs.move(params['picTmpPath'][i], params['picPath'][i]);
                            fs.copy(params['picTmpPath'][i], params['picPath'][i]);
                            console.log('copy complete: picTmpPath= "' + params['picTmpPath'][i] + '", picPath="'
                                    + params['picPath'][i]);
                            console.log("---------------------------------------");
                        } catch (e)
                        {
                            console.log("fs.move() Err: " + e);
                            errExist();
                        }
                    }
                }

                console.log("finish! phantom.exit...")
                phantom.exit();
            });
        } catch (e)
        {
            console.log("render() Err :" + e);
            errExist();
        }
    };

    try
    {
        setTimeout(function() 
        {
            // 防止在后面执行中存在问题，导致phantom始终占用，且且调用程序->java持续等待，无法继续正常执行
            console.log("wait too long, will do phantom exist...")
            phantom.exit();
        }, 3 * 60 * 1000);

        // get the args  
        var data = parseParams();
        render(data);

    } catch (e)
    {
        console.log("main() has catch Err:" + e)
        errExist();
    }

}());
