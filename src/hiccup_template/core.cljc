(ns hiccup-template.core
  (:require
   [clojure.string :as string]
   [clojure.walk :refer [postwalk]]))

;;; Hiccup parser ;;;

(defn normalize-body [body]
  (if (coll? body) (apply str (doall body)) (str body)))

(defn as-str
  "Converts its arguments into a string using to-str."
  [& xs]
  (apply str (map normalize-body xs)))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (-> (as-str text)
      (string/replace #"&" "&amp;")
      (string/replace #"<" "&lt;")
      (string/replace #">" "&gt;")
      (string/replace #"'" "&apos;")))

(defn xml-attribute [id value]
  (str " " (as-str (name id)) "=\"" (escape-html value) "\""))

(defn render-attribute [[name value]]
  (cond
    (true? value) (xml-attribute name name)
    (not value) ""
    :else (xml-attribute name value)))

(defn render-attr-map [attrs]
  (apply str (sort (map render-attribute attrs))))

(defn- merge-attributes [{:keys [id class]} map-attrs]
  (->> map-attrs
       (merge (if id {:id id}))
       (merge-with #(if %1 (str %1 " " %2) %2) (if class {:class class}))))

(defn normalize-element [[tag & content]]
  (let [re-tag    #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
        [_ tag id class] (re-matches re-tag (as-str (name tag)))
        tag-attrs {:id    id
                   :class (when class (string/replace class #"\." " "))}
        map-attrs (first content)]
    (if (map? map-attrs)
      [tag (merge-attributes tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(defn render-element [[tag attrs & content]]
  (str "<" (name tag) (render-attr-map attrs) ">" (as-str (flatten content)) "</" (name tag) ">"))

(defn render-hiccup [hiccup]
  (postwalk
   (fn [node]
     (if (and (not (map-entry? node)) (vector? node))
       (-> node normalize-element render-element)
       node))
   hiccup))

;;; template parser ;;;
  
(defn node-type [node]
  (cond
    (number? node) :number
    (string? node) :string
    (keyword? node) :keyword
    (map? node) :map
    (vector? node) :vector
    (list? node) :list
    (seq? node) :seq
    :else (keyword (type node))))

(defmulti parse-node (fn [type _ _] type))

(defn parse-path [path]
  (mapv keyword (string/split path #"\.")))

(defmethod parse-node :keyword [_ node data]
  (if (= "data" (namespace node))
    (get-in data (parse-path (name node)))
    node))

(defmethod parse-node :default [_ node _] node)

(defn hiccup [template data]
  (postwalk #(parse-node (node-type %) % data) template))

(defn html [template data]
  (render-hiccup (hiccup template data)))
