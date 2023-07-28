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
   ;; day 4
   "https://itch.io/jam/380910/entries.json"])

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

(defn entry->game [{:keys [game] :as entry}]
  (assoc (select-keys game [:title :url])
         :user (get-in game [:user :name])
         :entry-url (str "https://itch.io" (:url entry))
         :links (game-links (game-page (:url game)))))

(defn jam-submissions [link]
  (mapv entry->game
        (-> (crawl link)
            :body
            (json/parse-string true)
            :jam_games)))

(comment
  (crawl "https://blowupthenoobs.itch.io/wouldyouratherday3")
  (game-links (game-page "https://yngvarr.itch.io/pass-the-game-day-3"))
  (def the-subs (jam-submissions (first jam-links)))
  )
