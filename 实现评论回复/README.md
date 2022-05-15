实现功能主要是回帖、使用 emojionearea 插件实现表情回复
<br>
更多详细信息可以参考我的博客: https://blog.csdn.net/wyf2017/article/details/124551466?spm=1001.2014.3001.5501
<br>
2022-5-15 更新下 无法加载表情
今天进行测试的时候发现我的输入框表情无法加载出来，然后排查了下问题，发现我之前用的那个CDN 加速器域名换了，在文章之前使用的是这个

```
<link rel="stylesheet" type="text/css" href="http://cdn.bootcss.com/emojione/2.1.1/assets/sprites/emojione.sprites.css">  
```

> 后面这个开源网站的域名换了所以需要重新写过:   
```
<link rel="stylesheet" type="text/css"  href="http://cdn.bootcdn.net/ajax/libs/emojione/2.1.1/assets/sprites/emojione.sprites.css">
```


但是这有个问题就是如果哪一天这个域名又换了我们需要重新配置，既然这样我还不如直接点直接把它的样式拷到我自己本地，然后写一个css自己调用  

> 我们在css下新建一个名为: emojione.sprites.css
```
输入这个网址: href="http://cdn.bootcdn.net/ajax/libs/emojione/2.1.1/assets/sprites/emojione.sprites.css
将这里面的css拷贝到 emojione.sprites.css
```

> 然后将里面的那个表情地址下载下来，也就是一张 png， 这个png的地址需要替换 emojione.sprites.css 里面的
```
https://cdn.bootcdn.net/ajax/libs/emojione/2.1.1/assets/sprites/emojione.sprites.png
这个是我自己的地址: background-image: url("/community/img/emojione.sprites.png")
参考下就可以
```

> 最后直直接导入自己的样式即可
```
 <link rel="stylesheet" th:href="@{/css/emojione.sprites.css}"/>
```
