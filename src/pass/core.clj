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
   {:day 4 :url "https://itch.io/jam/380910/entries.json"}
   {:day 3 :url "https://itch.io/jam/380909/entries.json"}
   ;; {:day 2 :url "https://itch.io/jam/380908/entries.json"}
   ;; {:day 1 :url "https://itch.io/jam/380876/entries.json"}
   ])

(defn game-page [url]
  (let [crawl-result (crawl url)]
    (if (= 404 (:status crawl-result))
      {}
      (-> crawl-result
          :body
          html/parse
          html/as-hickory))))

;; TODO I don't like that there're 2 selects and 2 mapvs
(defn game-links [page]
  (mapv #(-> % :attrs :href)
        (flatten (mapv #(s/select (s/tag :a) %)
                       (s/select (s/child (s/class "formatted_description")) page)))))

(defn entry->game [{:keys [game] :as entry} day]
  (assoc (select-keys game [:title :url])
         :day day
         :user (get-in game [:user :name])
         :entry-url (str "https://itch.io" (:url entry))
         :links (game-links (game-page (:url game)))))

;; TODO make one big :nodes and one big :index for all 6 jams
(defn jam-submissions [link day]
  (let [res
        (reduce (fn [acc value]
                  (let [game (entry->game value day)]
                    {:nodes (conj! (:nodes acc) game)
                     :index (assoc! (:index acc) (:url game) (:entry-url game))}))
                {:nodes (transient [])
                 :index (transient {})}
                (-> (crawl link)
                    :body
                    (json/parse-string true)
                    :jam_games))]
    [(persistent! (:nodes res)) (persistent! (:index res))]))

(defn crawl-data []
  #_"download all entries, while building the index"
  #_"build links, using entry-urls as keys")

(comment
  (crawl "https://blowupthenoobs.itch.io/wouldyouratherday3")
  (game-links (game-page "https://yngvarr.itch.io/pass-the-game-day-3"))
  (def the-subs
    (let [jam (first jam-links)]
      (jam-submissions (:url jam) (:day jam))))
  )
