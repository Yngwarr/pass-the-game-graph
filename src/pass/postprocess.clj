(ns pass.postprocess
  (:require [cheshire.core :as json]))

(defn read-json [path]
  (json/parse-string (slurp path) true))

(defn find-game [graph entry-url]
  (first (filter #(= entry-url (:entry-url %)) (:nodes graph))))

(defn count-lonely [graph]
  (count
    (filter
      (fn [entry]
        (and (= 1 (:day entry))
             (not-any? #(or
                          (= (:entry-url entry)
                             (:target %))
                          (= (:entry-url entry)
                             (:source %)))
                       (:links graph))))
      (:nodes graph))))

(defn ancestor-descendant [graph {t :target s :source}]
  (let [day-t (:day (find-game graph t))
        day-s (:day (find-game graph s))]
    (cond
      (= day-t day-s) nil
      (> day-t day-s) [s t]
      :else [t s])))

(defn median [arr]
  (let [size (count arr)
        sorted-arr (sort arr)]
    (when (> size 0)
      (if (odd? size)
        (nth sorted-arr (/ size 2))
        (/ (+ (nth sorted-arr (/ size 2))
              (nth sorted-arr (dec (/ size 2))))
           2)))))

(comment
  (def graph (read-json "docs/data.json"))
  (def links (->> (:links graph)
                  (mapv #(ancestor-descendant graph %))
                  (filterv some?)))
  (def links-by-day
    (reduce
      (fn [acc [a d]]
        (let [day-a (dec (:day (find-game graph a)))]
          (assoc-in
            acc
            [day-a a]
            (let [res (get-in acc [day-a a])]
              (if (nil? res) [d] (conj res d))))))
      [{} {} {} {} {} {}] links))
  (def descendants-num
    (mapv #(reduce-kv
             (fn [acc k v]
               (conj acc (count v))) [] %)
          links-by-day))
  (mapv median descendants-num)

  (sort (nth descendants-num 4))

  (keys graph)
  (find-game graph "https://itch.io/jam/day-6/rate/2195670")
  (-> graph :nodes first))
