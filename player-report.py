#!/usr/bin/python3

import csv
from datetime import date
from dateutil.relativedelta import relativedelta
import json
import os
import re
import sqlite3

DB_FILE = 'players.db'
HOME_DIR = '/Users/brianeaton/'

if os.path.exists(DB_FILE):
  os.remove(DB_FILE)
con = sqlite3.connect(DB_FILE)


def known_names():
  """Get a set of names we know are in netfoos."""
  print('Reading .netfoosnames')
  with open(f'{HOME_DIR}/.netfoosnames') as json_file:
    names = json.load(json_file)
    known = set()
    for name in names.values():
      fields = name.split(', ')
      known.add(f'{fields[1]} {fields[0]}')
    return known


def init_db():
  known = known_names()
  cur = con.cursor()
  cur.execute('''CREATE TABLE events (name text, location text, player text, date text)''')

  input_rows = []
  print('Reading netfoos.tsv')
  with open('netfoos.tsv') as tsv_file:
    read_tsv = csv.reader(tsv_file, delimiter='\t')
    row_num = 0
    for row in read_tsv:
      row_num += 1
      name, location, players, date_str = row
      # Skip header
      if row_num == 1 and name == 'Name':
        continue
      for player in players.split(','):
        player = re.sub(r' +', ' ', player)
        player = player.strip().rstrip()
        if not player:
          continue
        if player == 'Jose Hernandez':
          player = 'Jose Rodriguez'
        elif player == 'Alex O':
          player = 'Alex Orona'
        elif player == 'Ryan Patterson':
          player = 'Ryan Doyle'
        if not player in known:
          raise Exception(f'Unknown player "{player}"')
        record = (name, location, player, date_str)
        input_rows.append(record)

  for root, _, files in os.walk(f'{HOME_DIR}/Documents/foos/tournament-results'):
    for file in files:
      if not file.endswith('.json'):
        continue
      json_file_name = os.path.join(root, file)
      print(f'Loading {json_file_name}')
      with open(json_file_name, 'r') as json_file:
        try:
          tournament = json.load(json_file)
        except json.decoder.JSONDecodeError as e:
          raise RuntimeError(f'Error reading {file}') from e
        location = tournament['location']
        date_str = tournament['date']
        name = tournament['name']
        for player in tournament['players']:
          record = (name, location, player, date_str)
          input_rows.append(record)

  cur.executemany('INSERT INTO events VALUES(?, ?, ?, ?)', input_rows)
  con.commit()


def dump_db():
  for row in con.cursor().execute('''
      SELECT
        name, location, player, date
      FROM events
      WHERE date = 'Date'
  '''):
    pass

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


def player_engagement():
  with open('/tmp/player_engagement.tsv', 'w') as out_file:
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
  min_max_query = '''
       SELECT
          MIN(strftime('%Y', date)),
          MAX(strftime('%Y', date))
       FROM events;
  '''
  min_year = None
  max_year = None
  for row in con.cursor().execute(min_max_query):
    min_year = int(row[0])
    max_year = int(row[1])
  with open('/tmp/player_history.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player']
    year_queries = []
    for year in range(min_year, max_year + 1):
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


def recent_changes():
  with open('/tmp/recent_changes.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    header = ['Player', 'Prev 30 Days', 'Current 30 Days', 'Change', 'Type']
    writer.writerow(header)
    query = '''
      WITH last_60 AS (
        SELECT
          player,
          COUNT(DISTINCT date) FILTER (WHERE date < date('now','-30 day')) prev_30,
          COUNT(DISTINCT date) FILTER (WHERE date >= date('now','-30 day')) cur_30
        FROM events
        WHERE
          date > date('now','-60 day')
        GROUP BY player
      )
      SELECT
        player,
        prev_30,
        cur_30,
        cur_30-prev_30 change,
        (CASE
          WHEN cur_30 = 0 THEN 'LOST'
          WHEN prev_30 = 0 THEN 'GAINED'
          WHEN cur_30 > prev_30 THEN 'INCREASED'
          ELSE 'DECREASED'
        END) label
      FROM last_60
    WHERE
      prev_30 = 0 
      OR cur_30 = 0
      OR ABS(change) > 1
    ORDER BY change
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


def last_60():
  with open('/tmp/last_60.tsv', 'w') as out_file:
    writer = csv.writer(out_file, delimiter='\t')
    headers = ['Player', 'Days']
    writer.writerow(headers)
    query = '''
      SELECT
        player,
        COUNT(DISTINCT date) days
      FROM events
      WHERE
        date > date('now','-60 day')
      GROUP BY player
      ORDER BY days DESC
    '''
    for row in con.cursor().execute(query):
      writer.writerow(row)


init_db()
dump_db()
entries_per_month()
month_comparison()
entries_per_year()
player_engagement()
player_history()
recent_changes()
last_60()
