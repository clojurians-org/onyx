(ns onyx.windowing.aggregation
  (:refer-clojure :exclude [min max count conj]))

(defn set-value-aggregation-apply-log [state [t v]]
  (case t 
    :set-value v)) 

(defn conj-aggregation-apply-log [state [t v]]
  (case t
    :conj (clojure.core/conj state v)))

(defn conj-aggregation-fn-init [window]
  [])

(defn sum-aggregation-fn-init [window]
  0)

(defn count-aggregation-fn-init [window]
  0)

(defn average-aggregation-fn-init [window]
  {:sum 0 :n 0})

(defn conj-aggregation-fn [state window segment]
  [:conj segment])

(defn sum-aggregation-fn [state window segment]
  (let [k (second (:window/aggregation window))]
    [:set-value (+ state (get segment k))]))

(defn count-aggregation-fn [state window segment]
  [:set-value (inc state)])

(defn min-aggregation-fn [state window segment]
  (let [k (second (:window/aggregation window))]
    [:set-value (clojure.core/min state (get segment k))]))

(defn max-aggregation-fn [state window segment]
  (let [k (second (:window/aggregation window))]
    [:set-value (clojure.core/max state (get segment k))]))

(defn average-aggregation-fn [state window segment]
  (let [k (second (:window/aggregation window))
        sum (+ (:sum state)
               (get segment k))
        n (inc (:n state))]
    [:set-value {:n n :sum sum :average (/ sum n)}]))

(def conj
  {:aggregation/init conj-aggregation-fn-init
   :aggregation/fn conj-aggregation-fn
   :aggregation/apply-state-update conj-aggregation-apply-log})

(def sum
  {:aggregation/init sum-aggregation-fn-init
   :aggregation/fn sum-aggregation-fn
   :aggregation/apply-state-update set-value-aggregation-apply-log})

(def count
  {:aggregation/init count-aggregation-fn-init
   :aggregation/fn count-aggregation-fn
   :aggregation/apply-state-update set-value-aggregation-apply-log})

(def min
  {:aggregation/fn min-aggregation-fn
   :aggregation/apply-state-update set-value-aggregation-apply-log})

(def max
  {:aggregation/fn max-aggregation-fn
   :aggregation/apply-state-update set-value-aggregation-apply-log})

(def average
  {:aggregation/init average-aggregation-fn-init
   :aggregation/fn average-aggregation-fn
   :aggregation/apply-state-update set-value-aggregation-apply-log})
