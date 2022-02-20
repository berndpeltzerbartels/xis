# XIS #

XIS-Remote allows you to create your single-page web-application with Spring-backend easily by hiding the
commununication-layer, based on HTTP and socket.io, behind some simple abstractions. By using XIS-Remote, you will save
all the code you normally have to write for restful-services or sending websocket-messages, handling client state,
reducers, effects and so on. All these things are running behind the scenes as generated Java-code and generated
javascript on client-side. It's controlled by some powerful and intuitive annotations in conjunction with a very simple
template-design. Go on with this documentation or check out the example code to see how straight-forward it is.

## Components

In XIS-Remote, one of the main concepts is the component. It's a Java-class in conjunction with a piece of HTML. If you
are familar with Angular, you may already know it.

The HTML-file can be located inside the components java-package or in the resouce-folder. For a single component this
may look like this:

```
src
    main
        java
            com
                mydomain
                    myapp
                        xyz
                            Xyz.java
                            Xyz.html

```

Intention was, inside your Java-code you feel like to be on client-side, but of course you are not. Getting values from
the page or placing data on it is done by socket.io automatically.

As a consequence, a component is not a singleton (because we have several clients). It is just in request scope (to keep
your server-application stateless), but it contains the actual client-state (by using @Binding- annotated fields).

In XIS-Remote, we have two kinds of components. Pages and widgets on one hand side and page-components on the other.

### Pages and Widgets

When you create a component to be loaded by an HTTP-URL, you have to annotate the java-classes with @Page or @Widget,
where in fact @Widget is just an alias for @Page, just to show, the corresponding HTML is just a fragment and intended
to be diplayed on an HTML-page. With the annotation, you define an URL to access such kind of components.

#### Widget

A widget has to be annotated wiht @Widget. There are several ways to load a some HTML at runtime and we will discuss it
later.

Example: WeatherWidget.java

```
@Widget("/weather/weather.widget")
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

The associated HTML.

WeatherWidget.html:

```
<div>
    <h4>${weather.location}</h4>
    <span>${degreesCentigrade} °</span>
    <span>${clouds}</span>
    <img src="${icon}"/>
</div>

```

#### Pages

Because a page is something like a root element, it is responsible to load the generated javascript. URL is
`/resources/public/xis-remote-generated.js`.

MainPage.html

```
<html>
    <head>
        <script type="text/javascript" src="/resources/public/xis-remote-generated.js"></script>
         <link rel="stylesheet" href="/resources/public/styles.css"> 
    </head>
    <body>
        <div data-id="widget"/>
        <div data-id="mainContent"/>
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

The setters allow set change the content of the two div-containers. The parameter has to be a page-component. You might
also use page-components inside page-components.

### Page-Components

Sorrily "@Component" is used by Spring. To avoid confusion, I named it @PageComponent. In contrary to pages or widgets,
the page-component is not associated with a URL, because it's loaded dynamically by the java-code of a page or widget or
an upper page-component in the way you know from MainPage-example above.

In this example, the main-page displays the weather-widget and the news-component, initially. As you see, a component
instance can be uses as a parameter in annotated methods. Bounded fields of the parameters (@Binding) will contain the
actual values from client.

**Instantiating page-components on your own is not recommended (no injection).**

If the value of a field annotated with @Binding is a Page-Component (annotated with @PageComponent), it will be
displayed in the bounded container. Before rendering, all methods of these components annotated with @OnInit are called.

A component (@PageComponent) might contain other components in the same way.
