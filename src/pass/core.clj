(ns pass.core
  (:require [clj-http.client :as http]
            [hickory.core :as html]
            [hickory.select :as s]
            [cheshire.core :as json]))

(defn crawl [url]
  (println (str "Crawling the page " url "..."))
  (http/get url {:throw-exceptions false}))

(def jam-links
  [
   ;; {:day 6 :url "https://itch.io/jam/380912/entries.json"}
   ;; {:day 5 :url "https://itch.io/jam/380911/entries.json"}
   {:day 4 :url "https://itch.io/jam/380910/entries.json"}
   {:day 3 :url "https://itch.io/jam/380909/entries.json"}
   {:day 2 :url "https://itch.io/jam/380908/entries.json"}
   {:day 1 :url "https://itch.io/jam/380876/entries.json"}
   ])

(defn game-page [url]
  (let [crawl-result (crawl url)]
    (if (= 404 (:status crawl-result))
      {}
      (-> crawl-result
          :body
          html/parse
          html/as-hickory))))

(def href-regex #"https:\/\/.+")

(defn extract-link-address [link-element]
  (def the-le link-element)
  (let [text (-> link-element :content first)
        href (-> link-element :attrs :href)]
    (if (and (string? text)
             (re-matches href-regex text))
      text
      href)))

(defn extract-links [page]
  (->> (s/select (s/child (s/class "formatted_description")) page)
       (mapv #(s/select (s/tag :a) %))
       flatten
       (mapv extract-link-address)))

(defn entry->game [{:keys [game] :as entry} day]
  (assoc (select-keys game [:title :url])
         :day day
         :user (get-in game [:user :name])
         :entry-url (str "https://itch.io" (:url entry))
         :links (extract-links (game-page (:url game)))))

(defn extract-entries [nodes index day entries]
  (reduce (fn [[nodes index] value]
            (let [game (entry->game value day)]
              [(conj! nodes game)
               (assoc! index (:url game) (:entry-url game))]))
          [nodes index]
          entries))

(defn jam-submissions []
  (->> jam-links
       (reduce (fn [[nodes index] {:keys [url day]}]
                 (extract-entries
                   nodes index day
                   (-> (crawl url)
                       :body
                       (json/parse-string true)
                       :jam_games)))
               [(transient []) (transient {})])
       (mapv persistent!)))

(def jam-link-regex #"https:\/\/itch.io\/jam\/day-\d\/rate\/\d+")

(defn previous-entry-link [game index]
  (first
    (reduce
      (fn [acc value]
        (cond
          (not (string? value))
          acc

          (and (not= (:entry-url game) value)
               (re-matches jam-link-regex value))
          (conj acc value)

          (get index value)
          (conj acc (get index value))

          :else acc))
      (sorted-set-by #(compare %2 %1))
      (:links game))))

(comment
  (previous-entry-link
    {:links ["https://theterrificjd.itch.io/bus-chase-city"
             "https://devcaty.itch.io/rouge-bus"
             "https://octrs.itch.io/rogue-taxi"]}
    {"https://theterrificjd.itch.io/bus-chase-city"
     "https://itch.io/jam/day-3/rate/2190718"

     "https://devcaty.itch.io/rouge-bus"
     "https://itch.io/jam/day-4/rate/2190718"}))

(defn make-links [nodes index]
  (persistent!
    (reduce (fn [acc game]
              (let [target (previous-entry-link game index)]
                (if target
                  (conj!
                    acc
                    {:source (:entry-url game)
                     :target target})
                  acc)))
            (transient []) nodes)))

(defn crawl-data []
  (let [[nodes index] (jam-submissions)]
    {:nodes nodes
     :links (make-links nodes index)}))

(defn build-json [data]
  (json/generate-string data {:pretty true}))

(defn -main [& _args]
  (->> (crawl-data)
      build-json
      (spit "public/data.json")))

(comment
  (-main)

  (crawl "https://blowupthenoobs.itch.io/wouldyouratherday3")
  (extract-links (game-page "https://yngvarr.itch.io/pass-the-game-day-3"))
  (extract-links (game-page "https://shadoweeq.itch.io/rogue-taxi-day4-unity"))
  (def the-subs
    (jam-submissions))
  (println (build-json the-subs))
  (count (first the-subs))
  (count (second the-subs)))
