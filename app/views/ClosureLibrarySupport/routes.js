*{ Make the Play routes available from the Javascript world }*
*{ @param namespace: String }*
*{ @param routes: List[ActionDefinition] routes to export }*

goog.provide('${namespace}');

${namespace}._computeUrl_ = function(pattern, args) {
  for (var i = 0 ; i < args.length ; i++) {
    pattern = pattern.replace(':'+i+':', args[i]);
  }
  return pattern;
};

#{list routes}
  %{ route = namespace + '.' + _.action }%
  goog.provide('${route}');
  ${route} = function() {
    return {
      url: ${_.args ? namespace + "._computeUrl_('" + _.url.raw() + "', arguments)" : "'" + _.url.raw() + "'"},
      method: '${_.method}'
    };
  };
#{/list}
