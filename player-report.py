#!/usr/bin/python3

import csv
from datetime import date
from dateutil.relativedelta import relativedelta
import sqlite3

con = sqlite3.connect(':memory:')

def init_db():
  cur = con.cursor()
  cur.execute('''CREATE TABLE events (name text, location text, player text, date text)''')

  input_rows = []
  with open('airtable.tsv') as tsv_file:
    read_tsv = csv.reader(tsv_file, delimiter='\t')
    for row in read_tsv:
      name, location, players, date = row
      for player in players.split(','):
        input_rows.append((name, location, player, date))

  cur.executemany('INSERT INTO events VALUES(?, ?, ?, ?)', input_rows)
  con.commit()


def entries_per_month():
  with open('/tmp/entries_per_month.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    writer.writerow(['Month', 'Entries'])
    cur = con.cursor()
    next_month = None
    for row in cur.execute('''
      SELECT
        strftime('%Y-%m', date, 'start of month') month,
        COUNT(player) entries FROM events
      GROUP BY month
      ORDER BY month
  '''):
      month = date.fromisoformat(f'{row[0]}-01')
      while next_month is not None and next_month < month:
        writer.writerow([next_month.isoformat(), 0])
        next_month = next_month + relativedelta(months=1)
      writer.writerow([month.isoformat(), row[1]])
      next_month = month + relativedelta(months=1)


def month_comparison():
  date2val = {}
  first = None
  last = None
  for row in con.cursor().execute('''
    SELECT
      strftime('%Y-%m', date, 'start of month') month,
      COUNT(player) entries FROM events
    GROUP BY month
    ORDER BY month
  '''):
    month = row[0]
    if first == None:
      first = month
    last = month
    date2val[month] = row[1]

  with open('/tmp/month_comparison.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    # Write header: Year, Jan, Feb, ...
    columns = ['Year']
    for month in range(1, 13):
      cal_date = date(2021, month, 1)
      columns.append(cal_date.strftime('%b'))
    writer.writerow(columns)
    first_month = date.fromisoformat(f'{first}-01')
    last_month = date.fromisoformat(f'{last}-01')
    first_year = first_month.year
    last_year = last_month.year
    for year in range(first_year, last_year + 1):
      row = [year]
      for month in range(1, 13):
        as_date = date(year, month, 1)
        if as_date < first_month or as_date > last_month:
          row.append('')
        else:
          row.append(date2val.get(as_date.strftime('%Y-%m'), 0))
      writer.writerow(row)


def entries_per_year():
  with open('/tmp/entries_per_year.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    writer.writerow(['Year', 'Entries'])
    cur = con.cursor()
    next_year = None
    for row in cur.execute('''
      SELECT
        strftime('%Y', date) year,
        COUNT(player) entries FROM events
      GROUP BY year
      ORDER BY year
  '''):
      year = date.fromisoformat(f'{row[0]}-01-01')
      while next_year is not None and next_year < year:
        writer.writerow([next_year.isoformat(), 0])
        next_year = next_year + relativedelta(years=1)
      writer.writerow([year.isoformat(), row[1]])
      next_year = year + relativedelta(years=1)


def player_activity():
  with open('/tmp/player_activity.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    writer.writerow(['Year', 'Total', 'Once', '2-6 Times', '7-12 Times', '13-24 Times', '25-50 Times', '50+ Times'])
    for row in con.cursor().execute('''
      WITH player_entries AS (
        SELECT
          strftime('%Y', date) year,
          player, 
          COUNT(date) entries
        FROM events
        WHERE player != ''
        GROUP BY year, player
        ORDER BY year, player
      ) SELECT
        year,
        SUM(1) total,
        SUM(CASE WHEN entries <= 1 THEN 1 ELSE 0 END) once,
        SUM(CASE WHEN entries > 1 AND entries <= 6 THEN 1 ELSE 0 END) range2_6,
        SUM(CASE WHEN entries > 6 AND entries <= 12 THEN 1 ELSE 0 END) range7_12,
        SUM(CASE WHEN entries > 12 AND entries <= 24 THEN 1 ELSE 0 END) range13_24,
        SUM(CASE WHEN entries > 24 AND entries <= 50 THEN 1 ELSE 0 END) range25_50,
        SUM(CASE WHEN entries > 50 THEN 1 ELSE 0 END) range50plus
      FROM player_entries
      GROUP BY year
      ORDER BY year
  '''):
      writer.writerow(row)


def player_history():
  with open('/tmp/player_history.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player']
    year_queries = []
    for year in range(2012, 2022):
      headers.append(year)
      #year_queries.append(f'SUM(entries) FILTER (WHERE year = "{year}")')
      year_queries.append(f'SUM(CASE WHEN year = "{year}" THEN entries ELSE 0 END)')
    writer.writerow(headers)
    query = '''
      WITH player_entries AS (
        SELECT
          strftime('%Y', date) year,
          player,
          COUNT(date) entries
        FROM events
        WHERE player != ''
        GROUP BY year, player
        HAVING entries > 0
        ORDER BY year, player
      ) SELECT
        player,
      ''' + ',\n'.join(year_queries) + '''
      FROM player_entries
      GROUP BY player
      ORDER BY player
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


def lost_players():
  with open('/tmp/lost_players.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player', 'Pre-2020', 'Post-2020']
    writer.writerow(headers)
    query = '''
      WITH change AS (
        SELECT
          player,
          SUM(CASE WHEN date < "2020-01-01" THEN 1 ELSE 0 END) pre2020,
          SUM(CASE WHEN date >= "2020-01-01" THEN 1 ELSE 0 END) post2020
        FROM events
        GROUP BY player
      ) SELECT
        player, pre2020, post2020
      FROM change
      WHERE
        pre2020 >= 10
        AND post2020 <= 3
      ORDER BY pre2020 DESC
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


def active_players():
  with open('/tmp/active_players.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player', 'Pre-2020', 'Post-2020']
    writer.writerow(headers)
    query = '''
      WITH change AS (
        SELECT
          player,
          SUM(CASE WHEN date < "2020-01-01" THEN 1 ELSE 0 END) pre2020,
          SUM(CASE WHEN date >= "2020-01-01" THEN 1 ELSE 0 END) post2020
        FROM events
        GROUP BY player
      ) SELECT
        player, pre2020, post2020
      FROM change
      WHERE
        post2020 > 6
      ORDER BY post2020 DESC
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


def new_players():
  with open('/tmp/new_players.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player', 'Pre-2020', 'Post-2020']
    writer.writerow(headers)
    query = '''
      WITH change AS (
        SELECT
          player,
          SUM(CASE WHEN date < "2020-01-01" THEN 1 ELSE 0 END) pre2020,
          SUM(CASE WHEN date >= "2020-01-01" THEN 1 ELSE 0 END) post2020
        FROM events
        GROUP BY player
      ) SELECT
        player, pre2020, post2020
      FROM change
      WHERE
        pre2020 < 5 AND post2020 > 0
      ORDER BY post2020 DESC
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


init_db()
entries_per_month()
month_comparison()
entries_per_year()
player_activity()
player_history()
lost_players()
active_players()
new_players()
