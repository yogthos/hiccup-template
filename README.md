### hiccup-template

A minimal Clojure/Script library for Hiccup style templating.

[![NPM](https://nodei.co/npm/hiccup-template.png?mini=true)](https://www.npmjs.com/package/hiccup-template)

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.yogthos/hiccup-template.svg)](https://clojars.org/net.clojars.yogthos/hiccup-template)

### usage

```clojure
(require '[hiccup-template.core :as ht])

(def data
 {:person
  {:first-name "John"
   :last-name "Doe"}})

 (def template
   [:div
    [:label "first name " :data/person.first-name]
    [:label "last name " :data/person.last-name]])

(ht/hiccup template data)
; [:div [:label "first name " "John"] [:label "last name " "Doe"]]

(ht/html template data)
; <div><label>first name John</label><label>last name Doe</label></div>
```
