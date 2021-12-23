# XIS-Remote #

XIS-Remote allows you to create your single-page web-application with Java- or Groovy-backend easily and runs fine with
Spring. The commununication-layer is based on http and socket.io and completely done by this framwork. By using
XIS-Remote, you will save all the code you normally would write for restful-services or sending websocket-messages,
handling client state, reducers, effects and so on. All these things are running behind the scenes as generated
Java-code and generated javascript on client-side. This will be under your control by some powerful and intuitive
annotations in conjunction with a very simple template-design. Go on with this documentation or check out the example
code to see how simple it is.

### Components

In XIS-Remote, one of the main concepts is the component. It's a Java-class in conjunction with a piece of HTML. If you
are familar with Angular, you already know this way and some other ideas of XIS-Remote. I tries to use similar naming,
in this framework.

The HTML-file can be located inside the components java-package or in the resouce-folder. For a single component this
may look like this:

```
src
    main
        java
            com
                mydomain
                    myapp
                        weather
                            WeatherWidget.java
                            WeatherWidget.html

```

WeatherWidget.java:

 ```
@PageComponent
 class WeatherWidget {
    
    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private LocationService locationService;
    
    @Binding
    private String locationTitle;
    
    @Binding
    private String clouds;
    
    @Binding
    private String icon;
    
    @Binding
    @NumberFormat(maxFractionDigits=1, minFractionDigits=1)
    private float degreesCentigrade;
    
    @OnInit
    void init() {
        Location location = locationService.getLocation();
        locationTitle = location.getTitle();
        Weather weather = weatherService.getWeather(location.getId());
        clouds = weaher.getClouds();
        icon = weather.getIcon();
    }
    
 }
 
 ```

Sorrily @Component is used by Spring. To avoid confusion, I named it @PageComponent.

WeatherWidget.html:

```
<div>
    <h4>${weather.location}</h4>
    <span>${degreesCentigrade} °</span>
    <span>${clouds}</span>
    <img src="${icon}"/>
</div>

```

Intention was, inside your Java-code you feel like to be on client-side, but you are not. In the example above, the init
method is called if the user's browser diplays the waether-widget. The Java-code is not a able to access the
HTML-fragment, directly. The data with the @Binding-annotation is displayed by some javascript-code, and you do not have
to take care for it.

As a consequence, a component is not a sigleton (because we have several clients). It is just in request scope (to keep
your the server-application stateless), but it contains the actual client-state (@Binding-fields).

### Pages

As you micht have noticed, the weather-widget's HTML is only a fragement. It is intended to be displayed on a HTML-page.
In XIS-Remote, a page with @Page. It's like a component on top level. Main difference is, it can be accessed by an url.
Compoents might contain a hierachy if components in a real life web-application. As an example, it may be sufficient to
have one page with 2 div-containers to add some content:

MainPage.html

```
<html>
    <head>
        <script type="text/javascript" src="/resources/public/xis-remote-generated.js"></script>
         <link rel="stylesheet" href="/resources/public/styles.css"> 
    </head>
    <body>
        <div id="widget"/>
        <div id="mainContent"/>
    </body>
</html>
```

MainPage.java

```
@Page("/index.html")
public class MainPage {

    @Binding
    private Object widget;
    
    @Binding
    private Object mainContent;
      
    @OnInit
    void init(WeatherWidget weatherWidget, News news) {
        if (widget == null) widget = weatherWidget;
        if (mainContent == null) mainContent = news;
    }

    public void setWidget(Object widget) {
        this.widget = widget;
    }
    
    public void setMainContent(Object mainContent) {
        this.mainContent = mainContent;
    }
    
}
```

In this example, the main-page displays the weather-widget and the news-component, initially. As you see, a component
instance can be uses as a parameter in annotated methods. Bounded fields of the parameters (@Binding) will contain the
actual client-state.

**Instantiating page-components on your own is not recommended (no injection).**

If the value of a field annotated with @Binding is a Page-Component (annotated with @PageComponent), it will be
displayed in the bounded container. Before rendering, all methods of these components annotated with @OnInit are called.

A component (@PageComponent) might contain other components in the same way.
